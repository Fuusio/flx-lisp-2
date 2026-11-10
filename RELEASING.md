# Releasing FLX Lisp

The library module `:flx` is published to Maven Central through the
[Central Portal](https://central.sonatype.com) with the
[Gradle Maven Publish plugin](https://github.com/vanniktech/gradle-maven-publish-plugin).

Coordinates: `org.fuusio.flx:flx:<VERSION_NAME>`. The group and version are set in
`gradle.properties` (`GROUP`, `VERSION_NAME`); the POM is configured in `flx/build.gradle.kts`.

## One-time setup

1. **Namespace.** In the Central Portal, make sure the `org.fuusio` namespace is verified for your
   account (it is verified by a DNS TXT record on `fuusio.org`). Verifying `org.fuusio` also
   covers `org.fuusio.flx`.
2. **User token.** In the Central Portal, generate a user token (*View Account → Generate User
   Token*). It gives a username and a password.
3. **Signing key.** Maven Central requires signed artifacts. Create a GPG key and publish its
   public part to a key server:

   ```bash
   gpg --full-generate-key
   gpg --list-secret-keys --keyid-format SHORT          # the 8-character key id
   gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
   gpg --export-secret-keys --armor <KEY_ID>             # the in-memory key for CI
   ```

4. **Credentials.** Never commit them. For local publishing, put them in
   `~/.gradle/gradle.properties`:

   ```properties
   mavenCentralUsername=<token username>
   mavenCentralPassword=<token password>
   signingInMemoryKey=<armored private key, newlines as \n>
   signingInMemoryKeyId=<KEY_ID>
   signingInMemoryKeyPassword=<key passphrase>
   ```

   For GitHub Actions, add these repository secrets: `MAVEN_CENTRAL_USERNAME`,
   `MAVEN_CENTRAL_PASSWORD`, `SIGNING_KEY`, `SIGNING_KEY_ID`, `SIGNING_KEY_PASSWORD`.

## Releasing a version

1. Update `VERSION_NAME` in `gradle.properties` and the version in `README.md`.
2. Move the changes under a new version heading in `CHANGELOG.md`, with the release date.
3. Check the artifacts locally:

   ```bash
   ./gradlew :flx:testDebugUnitTest
   ./gradlew :flx:publishToMavenLocal
   ls ~/.m2/repository/org/fuusio/flx/flx/<VERSION_NAME>/
   ```

4. Commit, then create a GitHub release with the tag `v<VERSION_NAME>` (for example `v2.0.0`).
   The *Publish to Maven Central* workflow checks that the tag matches `VERSION_NAME`, runs the
   tests, uploads the signed artifacts and releases them.

   To publish from your own machine instead:

   ```bash
   ./gradlew :flx:publishAndReleaseToMavenCentral --no-configuration-cache
   ```

5. The release appears on Maven Central after the Central Portal has validated and published it,
   usually within half an hour. Its progress is shown under *Deployments* in the Central Portal.
