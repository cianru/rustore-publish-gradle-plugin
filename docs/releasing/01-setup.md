## Setup

1. Add your GPG key to your github profile - this is required
   for github to know that your commits and tags are "verified".
1. Configure git with your codesigning key; make sure it's the same as the one
   you use to sign binaries (i.e. it's the same one you adaded to gradle.properties):
   ```bash
   # Do this for the repo only
   git config user.email "your@email.com"
   git config user.signingKey "your-key-id"
   ```
1. Add your plugins.gradle.org credentials to ~/.gradle/gradle.properties:
   ```gradle
   gradle.publish.key=<the key>
   gradle.publish.secret=<the secret>
   ```