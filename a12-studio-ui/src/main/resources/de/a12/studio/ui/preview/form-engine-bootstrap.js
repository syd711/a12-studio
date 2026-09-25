// Injected by PreviewServer into the Simple Model Editor client's index.html (served at /sme/).
//
// That client renders a Form Model with the real Form Engine when the window is named "preview-window": it asks
// its opener (the SME editor window) for data via postMessage and re-renders on every message it gets back (see
// moduleSupport/fmm in SME). There is no opener here, so this script plays that role: it answers the requests,
// fetching the Form Model, expanded Document Model and validation code from a12-studio, and pushes changes with the
// same message shapes SME uses ("set-initial-data", "update-models", "update-formModel").
(function () {
  "use strict";

  var sessionId = new URLSearchParams(location.search).get("session");
  var autoRefreshEnabled = __AUTO_REFRESH_ENABLED__;
  var refreshDelayMillis = __AUTO_REFRESH_DELAY_MILLIS__;

  var formRevision = null;
  var documentRevision = null;
  var initialDataRequested = false;
  var initialDataSent = false;
  var fetching = false;
  var banner = null;

  window.name = "preview-window";

  function toPreview(message) {
    message.direction = "sme->preview";
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
    var url = "/fe/" + encodeURIComponent(sessionId) + "/data?fm=" + encodeURIComponent(formRevision || "")
        + "&dm=" + encodeURIComponent(documentRevision || "");
    return fetch(url, { cache: "no-store" }).then(function (response) {
      return response.json().then(function (data) {
        if (!response.ok) { throw new Error(data.error || response.statusText); }
        return data;
      });
    });
  }

  function apply(data) {
    var titles = { headTitle: data.title, documentTitle: data.title };
    if (!initialDataSent) {
      toPreview({
        task: "set-initial-data",
        formModel: data.formModel,
        documentModel: data.documentModel,
        validationCode: data.validationCode,
        documentNames: [],
        themeNames: [],
        titles: titles
      });
      initialDataSent = true;
    }
    else if (data.documentModel !== undefined) {
      toPreview({
        task: "update-models",
        formModel: data.formModel,
        documentModel: data.documentModel,
        validationCode: data.validationCode,
        titles: titles
      });
    }
    else if (data.formModel !== undefined) {
      toPreview({ task: "update-formModel", formModel: data.formModel });
    }
    formRevision = data.formModelRevision;
    documentRevision = data.documentModelRevision;
  }

  function refresh() {
    if (!initialDataRequested || fetching) { return; }
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

  // What the preview window sends to its opener (see PreviewActions in fmm-support). Only the initial data request
  // matters here; documents and themes of the workspace are not offered, and edits made in the preview are not saved.
  var host = {
    postMessage: function (message) {
      if (message && message.task === "request-initial-data") {
        initialDataRequested = true;
        refresh();
      }
    }
  };
  Object.defineProperty(window, "opener", { configurable: true, value: host });

  if (autoRefreshEnabled) {
    setInterval(refresh, refreshDelayMillis);
  }
  else {
    // Without polling, the initial request is retried until it succeeds once.
    setInterval(function () { if (!initialDataSent) { refresh(); } }, 1000);
  }
})();
