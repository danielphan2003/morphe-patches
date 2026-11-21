# danielphan2003 Patches

Patches for [Morphe](https://morphe.software) that fix annoyances in Wattpad and Kiki Auto.

## Add these patches to Morphe

Open https://morphe.software/add-source?github=danielphan2003/morphe-patches on the device with
Morphe Manager, or add `https://github.com/danielphan2003/morphe-patches` as a patch source by hand.

## Patches

### Wattpad 11.40.5 (`wp.wattpad`)

| Patch | What it does |
| --- | --- |
| Disable telemetry and ad SDKs | Stops the ad networks and the analytics, attribution, crash reporting and session replay SDKs from starting, and only lets through the connections reading, comments and signing in need. Also lets Wattpad run with GrapheneOS's dynamic code loading via memory blocked. |
| Hide premium promos | Hides premium upsell buttons and banners, including the "No ads" button at the end of a part, and stops the subscription paywall from opening. |
| Hide reader ads | Hides ads in the reader, on story details and between comments, the empty ad slot over the bottom of the page, and the ad skips promo row. |
| Keep reader size steady | Keeps the reader the same size when the system bars appear or hide, so paging mode does not lay out the part again when the reading menu opens or the app is resumed. |
| Preload reader pages | Lays out the next 3 pages in paging mode ahead of time, so turning a page does not show a loading page. |
| Unlimited offline stories | Removes the limit on how many stories can be saved for offline reading. |
| Unlock theme colours | Unlocks every theme colour in Customize theme without a subscription. |
| Use Android share sheet | Opens Android's share sheet in place of Wattpad's own share grid, without the tracking parameters and username Wattpad adds to shared links. |

### Kiki Auto 26.04.01.02 (`ai.zalo.kiki.car`)

| Patch | What it does |
| --- | --- |
| Skip Zalo login | Uses Kiki's guest authentication instead of requiring a Zalo login. |
| Open YouTube in default app | Opens YouTube links in Android's default app instead of forcing the stock YouTube app. |
| Debug Kiki voice failures | Off by default. Logs voice recognition, auth and network failures to logcat. |

Each patch targets the app version listed above. Other versions are likely to fail to patch.

## Build

The Morphe Gradle plugin and patcher come from GitHub Packages, so Gradle needs a GitHub token
with the `read:packages` scope:

```sh
export GITHUB_ACTOR=<your GitHub username>
export GITHUB_TOKEN=<token with read:packages>
./gradlew :patches:buildAndroid generatePatchesList
```

Building the Kiki extension in `extensions/` also needs the Android SDK (`ANDROID_HOME`).
The patch bundle is written to `patches/build/libs/patches-<version>.mpp`.

## Releases

Commits on `dev` and `main` are released by [release.yml](.github/workflows/release.yml) with
semantic-release, so commit messages follow [Conventional Commits](https://www.conventionalcommits.org).
Do not edit the generated `patches-list.json`, `patches-bundle.json` or `CHANGELOG.md` by hand.

## License

Licensed under the [GNU General Public License v3.0](LICENSE), with the additional terms in [NOTICE](NOTICE).
This project is based on the [Morphe Patches template](https://github.com/MorpheApp/morphe-patches-template)
and is not affiliated with the Morphe project.
