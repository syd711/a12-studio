# overall dev_tutorial_frontend_middleware_and_saga

Task 5 - Middleware and Saga
WARNING This tutorial refers to an older version of A12 (2025.06-ext5). An updated
version is currently in progress and will be available as soon as possible.
NOTE This tutorial uses A12 version 2025.06-ext5 and is based on the Project Template
version 202506.5.1.
Prerequisites
IMPORTANT
If you are new to the development tutorials, make sure to first go through
Tutorials > General Information and Tutorials > Frontend > Introduction
before continuing here.
You can then check out the tag 2025.06-ext5/frontend/task-5-start to follow along with this
tutorial.
If you get stuck at any point, you can check out the tag 2025.06-ext5/frontend/task-5-end to see
how your code differs from the solution.
Use-Case
As it is very important for our users to reload the overview without reloading the entire
application, they would like to have a button to reload the overview. Furthermore, they want to
receive feedback whenever a data reload occurs.
To achieve this, we will add a button to the contact overview. When pressed, it will trigger a reload
of the overview data.
End Result
After finishing this task, you will know for middlewares and sagas:
• How to implement them.
• How to register them in A12.
• When to choose which.
Step-by-Step Instructions
Before we begin, let us outline the features we want to add using the following acceptance criteria:
1. Display a button in the overview with the sync icon.
◦ When the button is pressed, the overview will reload its data.
1

-- 1 of 9 --

2. Display a toast with a title and a message indicating whether the reload was successful or not.
We will start by adding the button to the Overview Model. Then, we will create a middleware to
listen for the overview button event and trigger the reload. After that, we will create a saga to
detect reloads and trigger a toast notification.
Add Button to the Overview Model
Your task:
• Add a button to the "Subheader" section in the "Custom Actions" tab of the Overview Model as
follows:
◦ Event: reload
◦ Icon: sync (filled)
◦ Label: "Reload"/"Neuladen"
▼ Click to see solution
Add a button to the Overview Model as follows:
IMPORTANT After making any changes to the models, remember to save them before
2

-- 2 of 9 --

restarting the server or deploying them in the SME.
Creating a Middleware
In order to react to the reload event from the button in the Overview Model, we will use a
middleware to dispatch additional actions and create our own application behavior.
But first, let us take a look at what middlewares are.
A middleware is a higher-order function that sits between dispatching an action and the moment it
reaches the reducer. It can inspect, modify, delay, cancel, or cause side effects in response to
actions.
Middlewares are composed into a chain; each middleware receives a next function that calls the
next middleware (or the reducers at the end). A middleware should normally call next(action) to
continue the chain, or it may short-circuit and not call next.
type Middleware<S = any, D extends Dispatch = Dispatch> = (api: MiddlewareAPI<D, S>)
=> (next: Dispatch) => (action: AnyAction) => any;
interface MiddlewareAPI<D extends Dispatch = Dispatch, S = any> {
dispatch: D;
getState(): S;
}
The middleware receives three conceptual parameters:
• the store, which exposes dispatch and getState so that middleware can read the current state
and dispatch additional actions;
• next, the function that passes the action to the next middleware or to the reducers;
• and the action itself. A middleware usually calls next(action) to continue the chain and often
returns the result of that call — this return value is important because callers may rely on it.
If a middleware intentionally intercepts or short-circuits an action, it may not call next, but such
behavior should be used deliberately and documented.
NOTE
• The order of middlewares can be important.
• Avoid mutating actions or state inside middleware.
• Always return the result of next(action) unless intentionally intercepting.
• Decide whether you need the state before or after processing the current action
and call getState accordingly.
• When dispatching from a middleware, add guards or conditions to avoid infinite
loops or unintended repeated handling.
A12 provides a helper function for creating middlewares for convenience.
3

-- 3 of 9 --

import { StoreFactories } from "@com.mgmtp.a12.client/client-core";
const middleware = StoreFactories.createMiddleware((api, next, action) => {
// ...
});
A basic implementation of a middleware using the createMiddleware helper looks like this:
const myMiddleware = StoreFactories.createMiddleware((api, next, action) => {
const stateBeforeNext = api.getState();
const result = next(action);
if(action.type === "MY_ACTION") {
const stateAfterNext = api.getState();
// Do something
}
return result;
});
Middleware can be registered as a global middleware in the appsetup.ts or as a part of the client
module (Client > Modularization). In both cases the middleware will behave the same. The only
difference is that you can only guarantee the ordering of middlewares if they are registered
globally. This is important if you want to manipulate actions before they hit the reducer.
Your task:
The functionality we are implementing is very general, so we will implement it not as separate
modules but as part of the application.
1. Create the middleware ReloadMiddleware that will implement the following behavior:
◦ Add a new folder called middlewares in client/src with a new file named
reloadMiddleware.ts.
◦ In this file, create a basic middleware as shown above.
◦ Use OverviewEngineActions and Events to identify the right action to react to.
◦ Dispatch ActivityActions.reloadData for the activity provided by the action.
2. Register the ReloadMiddleware in the appsetup.ts.
▼ Click to see solution
Step 1:
File: client/src/middlewares/reloadMiddleware.ts
export const ReloadMiddleware = StoreFactories.createMiddleware((api, next, action)
=> {
4

-- 4 of 9 --

const result = next(action);
if (
OverviewEngineActions.event.match(action) &&
Events.onEventButtonClicked.match(action.payload.engineAction) &&
action.payload.engineAction.payload.event === "reload"
) {
api.dispatch(ActivityActions.reloadData({ activityId:
action.payload.activityId }));
}
return result;
});
Step 2:
File: client/src/appsetup.ts
// ...
import { ReloadMiddleware } from "./middlewares/reloadMiddleware";
// ...
export function setup() {
// ...
const applicationFeatures = combineFeatures(
viewAndLayoutFeatures,
addAdditionalMiddlewares(
ReloadMiddleware,
registerModulesOnSetModelGraphMiddleware,
unregisterModulesOnLogoutMiddleware
),
// ...
);
// ...
}
Creating a Saga
To detect a reload, we will use a saga because this involves asynchronously waiting for the data to
be reloaded.
But first, let us look at what sagas are.
Redux-Saga is a middleware that manages side effects — such as asynchronous calls, delays,
websockets, and other imperative interactions — by using ES2015 generator functions that are
called "sagas". Instead of performing side effects directly inside action creators or reducers, sagas
5

-- 5 of 9 --

yield plain effects (created by helpers like take, takeEvery, takeLatest, call, put, select) that the
middleware interprets and executes. This approach makes the side-effect logic declarative, easier to
read, and simpler to test because a saga’s yielded effects can be inspected without actually running
network requests or other external operations. For more details see Client > Asynchronous Flows
With Redux Saga.
type Saga<A extends unknown[], R> = (...args: A) => SagaGenerator<R>;
To obtain strongly typed results for the used effects, it is recommended to use typed-redux-saga.
typed-redux-saga is a small helper library that provides strongly typed wrappers around the
standard Redux-Saga effect creators. The library’s helpers (take, takeEvery, takeLatest, call, put,
select, etc.) are typed so that when you use them with yield* the compiler knows the exact types of
the values returned by the effects. This avoids the common need to fall back to SagaIterator or
brittle Generator<R> unions and makes sagas easier to use and refactor with compile-time safety.
For an overview of all effects, please consult the Redux-Saga documentation at https://redux-
saga.js.org/docs/api#effect-creators.
NOTE
Sagas operate after the reducers, unlike simple middlewares which operate
between dispatching an action and the moment it reaches the reducer. As a
consequence, it is not as trivial to access the store state before an action is
processed.
IMPORTANT
Due to the asynchronous nature of sagas, it may be necessary to select data
multiple times from the store because the data may change over the lifetime
of a saga’s execution. For example, an activity may exist at the beginning of a
saga but not at the end. Your code must take this into account.
A basic usage of a saga looks like this:
function* mySaga(): SagaGenerator<void> {
yield* takeEvery("MY_ACTION", function* handleMyAction(action) {
const obj = yield* select(myStateSelector);
// Do something
});
}
IMPORTANT
Redux-Saga does not provide an integrated error handling for effects like
takeEvery. It behaves like a while-true-loop. If an error is thrown, then the
loop is exited, otherwise it will go on forever. Therefore, you should always
think about thrown errors of the called code.
Additionally, for your convenience A12 client provides StoreSagas that you can utilize in your own
custom saga to suspend it until the selector handed over yields any defined value (waitFor) or a
specific value (waitForStateChange). The expected selector type differs a little bit between these two
methods.
6

-- 6 of 9 --

A12 exposes two extension points on the appsetup.ts for registering sagas globally:
• addCustomSagas(saga1, saga2, …) — registers application-specific sagas additively. This is the
right choice for behavior that is not part of A12’s built-in flow, such as our reload notification.
• addPlatformSagas(descriptor1, descriptor2, …) — registers handlers
(ApplicationSaga.Descriptor) that override A12’s built-in dispatcher behavior.
NOTE Platform-sagas should only be used when you want to override A12 built-in
behavior. For more details see Client > Connecting Sagas to the Dispatcher.
Sagas can also be registered as a part of the client module (Client > Modularization). In contrast to
middlewares, sagas behave the same in both cases because they do not have a real ordering.
Your task:
The functionality we are implementing is very general, so we will implement it as part of the
application.
NOTE
The localization keys for the toast have already been added for you in the starting
commit. The following entries have been added to client/src/localization/keys.ts,
en_US.ts, and de_DE.ts respectively.
client/src/localization/keys.ts:
notification: {
reload: {
success: {
title: "",
message: ""
},
error: {
title: "",
message: ""
}
}
},
1. Create the saga ReloadNotificationSaga that will implement the behavior:
◦ Add a new file named reloadNotificationSaga.ts in client/src/sagas.
◦ In this file, create a basic saga as shown above.
◦ Use takeLatest to react to the ActivityActions.reloadData action.
◦ Use StoreSagas.waitFor to wait for the loading state to be "loaded" or "error".
◦ Dispatch, with put, a NotificationActions.add action.
▪ Use the keys added to RESOURCE_KEYS for the title and message properties.
▪ Set the correct severity (use "success" for "loaded" and "error" for "error").
7

-- 7 of 9 --

▪ Add the activity id to the toast so that the activity and toast are connected.
2. Register the ReloadNotificationSaga in appsetup.ts.
▼ Click to see solution
Step 1:
File: client/src/sagas/reloadNotificationSaga.ts
import { put, type SagaGenerator, takeLatest } from "typed-redux-saga";
import { ActivityActions, ActivitySelectors, NotificationActions, StoreSagas } from
"@com.mgmtp.a12.client/client-core";
import { RESOURCE_KEYS } from "../localization";
export const ReloadNotificationSaga = function* reloadNotificationSaga():
SagaGenerator<void> {
yield* takeLatest(ActivityActions.reloadData, function* (action) {
const loadingStateSelector =
ActivitySelectors.loadingStateById(action.payload.activityId);
const state = yield* StoreSagas.waitFor((state) => {
switch (loadingStateSelector(state)) {
case "loaded":
return "success";
case "error":
return "error";
default:
return undefined;
}
});
yield* put(
NotificationActions.add({
activityId: action.payload.activityId,
title: { key: RESOURCE_KEYS.notification.reload[state].title },
message: { key: RESOURCE_KEYS.notification.reload[state].message },
severity: state
})
);
});
};
Step 2:
File: client/src/appsetup.ts
// ...
import { ReloadNotificationSaga } from "./sagas/reloadNotificationSaga";
8

-- 8 of 9 --

// ...
export function setup() {
// ...
const applicationFeatures = combineFeatures(
// ...
addCustomSagas(ReloadNotificationSaga, LoadModelGraphSaga),
// ...
);
// ...
}
Conclusion
In this task, you have learned how to use middlewares and sagas and gained some initial
experience working with them.
If you got stuck at any point, you can check out 2025.06-ext5/frontend/task-5-end to see how your
code differs from the solution.
NOTE
Recommendation: When to use middleware vs. sagas
• Use middleware when the behavior is simple, not a flow, and not asynchronous;
they have minimal overhead and are easier to debug.
• Use sagas for flows or asynchronous behavior, especially when multiple
middlewares would need to coordinate or depend on each other; sagas improve
maintainability.
« Task 4: Data in Activities Task 6: Unit Testing »
9

-- 9 of 9 --

