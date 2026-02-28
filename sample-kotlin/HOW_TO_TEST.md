```bash
./gradlew :sample-kotlin:bundleRelease publishRustoreRelease --apiStub
./gradlew clean :sample-kotlin:bundleRelease publishRustoreRelease --apiStub
./gradlew :sample-kotlin:bundleRelease publishRustoreRelease --apiStub --refresh-dependencies
./gradlew :sample-kotlin:bundleRelease publishRustoreRelease --buildFormat=apk --apiStub
./gradlew :sample-kotlin:bundleRelease publishRustoreRelease --buildFormat=apk --apiStub --info
```