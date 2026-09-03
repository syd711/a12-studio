# utils_logging_collections utils collections documentation

Collections
The package @com.mgmtp.a12.utils/utils-collections contains collection types
with Set and Map semantics.
While standard JavaScript objects as well as ES6 Maps work fine as long as primitive keys are used,
they do define key identity. This means, whenever two objects that share the same identity are used
as key of a Map or an JS object, they are treated as two distinct entities (for Map this is due to same
value zero comparison for key instances).
That being said, this collections library is of use to you in case:
1. you are looking for a map data structure that you want to use with your own complex data
types as key
2. you are looking for a set with the same thoughts in mind
3. you are looking for a sorted set that also allows to use complex values
In those circumstances this collection library can help you.
Breaking Change Management
The utils-collections package follows the general A12 breaking change interpretation.
Migration Instructions
2025.06-ext4
Deprecation
Deep import paths of A12 Collections npm package
Nested imports from the npm package @com.mgmtp.a12.utils/utils-collections (e.g.
@com.mgmtp.a12.utils/utils-collections/lib/ComparativeSortedSet.js) are now deprecated in favor
of top-level imports to avoid unnecessary breaking changes and reduce ongoing maintenance
effort.
The ability to use the old-style ("long") imports will be removed in the next breaking release.
To migrate, first install or update the codemod with your preferred package manager, e.g. with
pnpm:
pnpm install -g @com.mgmtp.a12.utils/utils-collections-codemod
Then run the specific recipe:
1

-- 1 of 2 --

utils-collections-codemod preferTopLevel <path to directory with tsconfig.json>
// or as a single command
pnpx @com.mgmtp.a12.utils/utils-collections-codemod preferTopLevel <path to directory
with tsconfig.json>
For further details, run the codemod with the --help flag.
2025.06
Breaking Changes
Migration to ESM
The npm artifact @com.mgmtp.a12.utils/utils-collections was migrated from CommonJS to ESM.
When using Node 22.12+ and modern build tools, there should be no changes necessary to your
bundler setup.
Migrating your own application to ESM is not required, but recommended. Consult the
documentation of your bundler for specifics.
Updating to ES2024
The javascript output of the npm artifact was updated from ES2020 to ES2024 to be able to use latest
language features. When using supported browsers, there is no change necessary. If support for
older browsers is required, make sure to include necessary polyfills.
Typo In DfsTopology method
The method hasCylce() of the DfsTopology class was corrected to hasCycle(). Please update the name
accordingly.
Removed Deprecated API
The deprecated method connectVertex() of the DirectedGraph class was removed. Please use
connectByVertex() instead, which accepts the same parameters.
2

-- 2 of 2 --

