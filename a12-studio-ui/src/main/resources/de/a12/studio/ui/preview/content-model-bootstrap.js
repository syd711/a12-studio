// Injected by PreviewServer into the Simple Model Editor client's index.html (served at /sme/, with ?content=<session>).
//
// That client renders a Content Model with the real Content Engine when the window's name contains
// "content-model-preview=true": it asks its opener (the SME editor window) for data via postMessage, pings it every
// 500 ms (and closes itself when there is no opener), and re-renders on every "send-data" message it gets back (see
// ContentModelPreviewWindow / ContentPreviewMessage in @com.mgmtp.a12.contentengine/contentengine-editor). There is
// no opener here, so this script plays that role: it answers the requests, fetching the Content Model (and the
// Document Model it is bound to, with its validation code) from a12-studio, and pushes changes with the same message
// shapes SME uses.
//
// The page is meant to run inside the JavaFX WebView of the Content Model editor as well as in a browser. The
// WebView's WebKit has no IndexedDB, which the client touches while booting, so a minimal in-memory stand-in is
// installed if it is missing.
(function () {
  "use strict";

  var sessionId = new URLSearchParams(location.search).get("content");
  var autoRefreshEnabled = __AUTO_REFRESH_ENABLED__;
  var refreshDelayMillis = __AUTO_REFRESH_DELAY_MILLIS__;

  var contentRevision = null;
  var documentRevision = null;
  var contentModel = null;
  var documentModel = null;
  var dataRequested = false;
  var sent = false;
  var dataWanted = false;
  var fetching = false;
  var banner = null;

  window.name = location.href + ";content-model-preview=true";

  // Script errors of the page, for whoever embeds it (the editor's WebView has no console to look at).
  window.__previewErrors = [];
  window.addEventListener("error", function (event) { window.__previewErrors.push(String(event.message)); });
  window.addEventListener("unhandledrejection", function (event) {
    window.__previewErrors.push("Unhandled rejection: " + (event.reason && event.reason.stack || event.reason));
  });

  try {
    installIndexedDbStandIn();
  }
  catch (error) {
    window.__previewErrors.push("IndexedDB stand-in: " + error);
  }

  // The preview window's chrome is not offered: the editor embeds the page as a plain rendering of the Content Model,
  // like the preview area of SME's editor. That leaves out the sidebar menu (width / theme / locale / data / validate),
  // the title bar ("[SME] Content Model Preview" and the version), the "Preview" heading of the box around the
  // rendering, and the box's border and the margin around it.
  var hideChrome = document.createElement("style");
  hideChrome.textContent = "[data-role='application-frame-sidebar-wrapper']," +
      "[data-role='application-frame-toggle-sidebar-button']," +
      "[data-role='application-frame-header']," +
      "[data-role='application-frame-main'] [data-role='split-view-area'] > [data-role='resizable-handler-wrapper']" +
      " > [data-role='contentbox'] > [data-role='contentbox-header']{display:none !important}" +
      "[data-role='application-frame-main']{padding:0 !important;margin:0 !important;width:100% !important;" +
      "max-width:100% !important}" +
      "[data-role='application-frame-main'] [data-role='split-view-area'] > [data-role='resizable-handler-wrapper']" +
      " > [data-role='contentbox']{box-shadow:none !important;border:0 !important;border-radius:0 !important}";
  (document.head || document.documentElement).appendChild(hideChrome);

  // ContentPreviewMessage.BaseMessage
  function toPreview(message) {
    message.origin = location.origin;
    message.pathname = location.pathname;
    message.preview = "content";
    window.postMessage(message, location.origin);
  }

  function showError(text) {
    if (!banner) {
      banner = document.createElement("div");
      banner.style.cssText = "position:fixed;left:0;right:0;bottom:0;z-index:2147483647;padding:10px 16px;" +
          "background:#b3261e;color:#fff;font:14px/1.4 sans-serif;white-space:pre-wrap";
      document.addEventListener("DOMContentLoaded", function () { document.body.appendChild(banner); });
      if (document.body) { document.body.appendChild(banner); }
    }
    banner.textContent = text;
    banner.hidden = false;
  }

  function clearError() {
    if (banner) { banner.hidden = true; }
  }

  function fetchSnapshot() {
    var url = "/cm/" + encodeURIComponent(sessionId) + "/data?cm=" + encodeURIComponent(contentRevision || "")
        + "&dm=" + encodeURIComponent(documentRevision || "");
    return fetch(url, { cache: "no-store" }).then(function (response) {
      return response.json().then(function (data) {
        if (!response.ok) { throw new Error(data.error || response.statusText); }
        return data;
      });
    });
  }

  // The preview window wants the Document Model as the kernel's own (deserialized) model object, plus the serialized
  // one it hands to the backend to generate the validation code; the backend of this session serves the serialized
  // form, so the kernel that the client bundle contains is asked to deserialize it.
  function deserializeDocumentModel(serialized) {
    var Factory = findDocumentServiceFactory();
    if (!Factory) { throw new Error("The Document Model cannot be read: the kernel is not part of the client bundle."); }
    var model = new Factory().getDocumentModelSerializer().deserialize(JSON.stringify(serialized.withoutMetaData));
    model.serializedDocumentModel = serialized.expanded;
    return model;
  }

  // The client bundle is one webpack build with minified export names. Its module factories are reachable through a
  // chunk registration, which hands out webpack's require function; the kernel's DocumentServiceFactory is the class
  // with a getDocumentModelSerializer method that the module defining it exports. Modules that cannot be required are
  // skipped.
  var documentServiceFactory = null;

  function findDocumentServiceFactory() {
    if (documentServiceFactory) { return documentServiceFactory; }
    var chunkList = Object.keys(window).filter(function (key) { return key.indexOf("webpackChunk") === 0; })[0];
    if (!chunkList) { return null; }
    window[chunkList].push([[Symbol("a12-studio-preview")], {}, function (webpackRequire) {
      Object.keys(webpackRequire.m || {}).some(function (id) {
        try {
          if (String(webpackRequire.m[id]).indexOf("getDocumentModelSerializer(){return new") < 0) { return false; }
          var exports = webpackRequire(id);
          return Object.keys(exports).some(function (name) {
            var candidate = exports[name];
            if (typeof candidate === "function" && candidate.prototype && candidate.prototype.getDocumentModelSerializer) {
              documentServiceFactory = candidate;
              return true;
            }
            return false;
          });
        }
        catch (ignored) {
          return false;
        }
      });
    }]);
    return documentServiceFactory;
  }

  function apply(data) {
    var changed = !sent || dataWanted;
    dataWanted = false;
    if (data.contentModel !== undefined) {
      contentModel = JSON.parse(data.contentModel);
      elementIds = {};
      collectElementIds(contentModel.content && contentModel.content.root);
      changed = true;
    }
    if (data.hasDocumentModel === false) {
      changed = changed || documentModel !== null;
      documentModel = null;
    }
    else if (data.documentModel !== undefined) {
      documentModel = deserializeDocumentModel({
        expanded: JSON.parse(data.documentModel),
        withoutMetaData: JSON.parse(data.documentModelWithoutMetaData)
      });
      changed = true;
    }
    contentRevision = data.contentModelRevision;
    documentRevision = data.documentModelRevision;

    // The client re-renders on every message, so it only gets one when there is something new.
    if (changed) {
      var message = { type: "send-data", contentModel: contentModel, themeNames: [], documentIds: [] };
      if (documentModel) { message.documentModel = documentModel; }
      toPreview(message);
      sent = true;
    }
  }

  function refresh() {
    if (!dataRequested || fetching) { return; }
    fetching = true;
    fetchSnapshot().then(function (data) {
      clearError();
      apply(data);
    }).catch(function (error) {
      showError("Preview not updated: " + error.message);
    }).then(function () {
      fetching = false;
    });
  }

  // What the preview window sends to its opener. Only the data request matters here; the workspace's sample documents
  // and themes are not offered, and edits made in the preview are not saved.
  var host = {
    postMessage: function (message) {
      if (message && message.type === "request-data") {
        dataRequested = true;
        dataWanted = true;
        refresh();
      }
    }
  };
  Object.defineProperty(window, "opener", { configurable: true, value: host });

  // Element selection. The Content Engine renders an element with its model id as the DOM id (elements that render
  // nothing of their own, like table rows, have none). When the page is embedded in the editor, the JavaFX side
  // provides window.studioSelectionBridge: a click in the page reports the model element under it, and the editor
  // calls window.studioSelect(path) - the ids from the selected element up to the root - to have a frame drawn around
  // the first of them that is rendered. Without the bridge (a plain browser) the page is left alone.
  var elementIds = {};
  var selectedPath = [];
  var frame = null;
  var frameUpdateScheduled = false;

  function collectElementIds(element) {
    if (!element) { return; }
    if (typeof element.id === "string") { elementIds[element.id] = true; }
    (element.children || []).forEach(collectElementIds);
  }

  function bridge() {
    return window.studioSelectionBridge || null;
  }

  document.addEventListener("click", function (event) {
    if (!bridge()) { return; }
    for (var node = event.target; node && node.nodeType === 1; node = node.parentNode) {
      if (node.id && elementIds[node.id]) {
        // The page only shows the model; following its links or submitting its buttons would leave the editor.
        event.preventDefault();
        event.stopPropagation();
        bridge().elementClicked(node.id);
        return;
      }
    }
  }, true);

  function renderedSelection() {
    for (var i = 0; i < selectedPath.length; i++) {
      var node = document.getElementById(selectedPath[i]);
      if (node) { return node; }
    }
    return null;
  }

  function updateFrame() {
    frameUpdateScheduled = false;
    var node = renderedSelection();
    if (!node) {
      if (frame) { frame.style.display = "none"; }
      return;
    }
    if (!frame) {
      frame = document.createElement("div");
      frame.style.cssText = "position:fixed;z-index:2147483646;pointer-events:none;box-sizing:border-box;" +
          "border:2px solid #1a73e8;box-shadow:0 0 0 1px rgba(255,255,255,.8),inset 0 0 0 1px rgba(255,255,255,.8);" +
          "background:rgba(26,115,232,.08)";
    }
    if (!frame.parentNode) { document.documentElement.appendChild(frame); }
    var rect = node.getBoundingClientRect();
    frame.style.display = "block";
    frame.style.left = rect.left + "px";
    frame.style.top = rect.top + "px";
    frame.style.width = rect.width + "px";
    frame.style.height = rect.height + "px";
  }

  function scheduleFrameUpdate() {
    if (frameUpdateScheduled || !selectedPath.length) { return; }
    frameUpdateScheduled = true;
    setTimeout(updateFrame, 30);
  }

  window.studioSelect = function (path) {
    var changed = String(path) !== String(selectedPath);
    selectedPath = path || [];
    updateFrame();
    var node = changed ? renderedSelection() : null;
    if (node && node.scrollIntoView) { node.scrollIntoView({ block: "nearest", inline: "nearest" }); }
    updateFrame();
  };

  // The frame follows the page: re-rendering after an edit, resizing and scrolling (also inside scroll containers).
  window.addEventListener("resize", scheduleFrameUpdate);
  window.addEventListener("scroll", scheduleFrameUpdate, true);
  new MutationObserver(function (records) {
    // Moving the frame itself is a mutation as well.
    if (records.some(function (record) { return record.target !== frame; })) { scheduleFrameUpdate(); }
  }).observe(document.documentElement,
      { childList: true, subtree: true, attributes: true, characterData: true });

  if (autoRefreshEnabled) {
    setInterval(refresh, refreshDelayMillis);
  }
  else {
    // Without polling, the initial request is retried until it succeeds once.
    setInterval(function () { if (!sent) { refresh(); } }, 1000);
  }

  // Just enough of IndexedDB for the client's start-up (it opens its "sme" database and reads the installed plugins,
  // of which a preview has none).
  function installIndexedDbStandIn() {
    if (window.indexedDB) { return; }

    function completed(request, result) {
      request.result = result;
      request.readyState = "done";
      setTimeout(function () { if (request.onsuccess) { request.onsuccess({ target: request }); } }, 0);
      return request;
    }

    var storeNames = [];
    var database = {
      objectStoreNames: {
        get length() { return storeNames.length; },
        contains: function (name) { return storeNames.indexOf(name) >= 0; }
      },
      createObjectStore: function (name) { storeNames.push(name); },
      transaction: function () {
        return {
          objectStore: function () {
            return {
              getAll: function () { return completed({ readyState: "pending" }, []); },
              put: function () { return completed({ readyState: "pending" }, undefined); },
              delete: function () { return completed({ readyState: "pending" }, undefined); }
            };
          }
        };
      }
    };

    var factory = {
      open: function () {
        var request = { readyState: "pending", result: database };
        setTimeout(function () {
          if (request.onupgradeneeded) { request.onupgradeneeded({ target: request }); }
          completed(request, database);
        }, 0);
        return request;
      }
    };
    // A getter-only property of window where the feature is compiled out, so assigning to it would not work.
    Object.defineProperty(window, "indexedDB", { configurable: true, value: factory });
  }
})();
