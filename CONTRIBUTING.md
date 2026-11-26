# Contributing

Bug reports and patch requests are welcome as [issues](https://github.com/danielphan2003/morphe-patches/issues).

## Bug reports

Open a [bug report](https://github.com/danielphan2003/morphe-patches/issues/new?labels=Bug+report&template=bug_report.yml&title=bug%3A+)
with the app version, the patches you applied and, if the app crashes, the `AndroidRuntime` lines from logcat.

## Feature requests

Open a [feature request](https://github.com/danielphan2003/morphe-patches/issues/new?labels=Feature+request&template=feature_request.yml&title=feat%3A+)
describing the app, its version and what the patch should change.

## Pull requests

1. Open an issue first to discuss larger changes.
2. Branch from `dev`, and use [Conventional Commits](https://www.conventionalcommits.org) for commit messages.
3. Open the pull request against `dev`, and mention the issues it closes.

## TikTok SEA patches

The TikTok patches and extensions come from [icysymmetra/tiktok-patches-for-morphe](https://github.com/icysymmetra/tiktok-patches-for-morphe)
at `v0.10.0` (commit `8759b348497f39f799baf1bc34015f4b51605b80`), ported to TikTok SEA 46.9.3 on
[danielphan2003/tiktok-patches-for-morphe](https://github.com/danielphan2003/tiktok-patches-for-morphe)
branch `feat/tiktok-sea` (commit `874c5d1a84ec89201884c9488632ceac620a1aee`). They keep upstream's paths and
package names, so a later upstream release can be compared and copied over file by file:

| Path | From upstream |
| --- | --- |
| `patches/src/main/kotlin/app/morphe/` | as is, `util/PatchListGenerator.kt` included (it replaced the template's) |
| `patches/stub/` | as is |
| `extensions/shared/` | as is |
| `extensions/tiktok/src/main/`, `extensions/tiktok/stub/` | as is |
| `extensions/tiktok/build.gradle.kts` | trimmed: upstream's also runs its Robolectric tests and screenshot tasks |
| `extensions/proguard-rules.pro` | upstream's rules appended to this repository's |

Upstream's tests, scripts, docs and release tooling are not carried. To bring in a newer release
`<tag>`, apply upstream's changes since the base to those paths and merge the conflicts with the SEA port:

```sh
git clone https://github.com/icysymmetra/tiktok-patches-for-morphe /tmp/upstream
git -C /tmp/upstream diff 8759b348497f39f799baf1bc34015f4b51605b80 <tag> -- \
  patches/src/main/kotlin/app/morphe patches/stub extensions/shared \
  extensions/tiktok/src/main extensions/tiktok/stub > /tmp/upstream.diff
git apply -3 /tmp/upstream.diff
```

Then update the base commit above, and patch TikTok SEA 46.9.3 with every patch that declares it
(`AppCompatibilities.tiktokAndSea()`): each has to apply, and the parts left out on SEA print a
"Left out" note. A patch that stops applying goes back to `AppCompatibilities.tiktok()` until it is
ported again.
