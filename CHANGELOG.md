# 0.4.0

##### Breaking Changes
* [issue#8](https://github.com/cianru/rustore-publish-gradle-plugin/issues/8) The companyId in POST /public/auth/ will be deprecated by new documentation.
  According to [message](https://t.me/rustoredev/476) the `companyId` parameter of `POST /public/auth/` request is deprecated from 30 Jule 2024. Need to use `keyId` instead of. 
  
  You need to get `keyId` by [instruction](https://www.rustore.ru/help/work-with-rustore-api/api-authorization-token/) and change the credentials file from:
  ```json
  {
     "company_id": "<COMPANY_ID>",
     "client_secret": "<CLIENT_SECRET>"
  }
  ```
  to:
  ```json
  {
     "key_id": "<KEY_ID>",
     "client_secret": "<CLIENT_SECRET>"
  }
  ```

# 0.3.2

##### Add
* [issue#5](https://github.com/cianru/rustore-publish-gradle-plugin/issues/5) Support of serviceType plugin param.


# 0.3.1

##### Add
* Support of Gradle 8+, Android Gradle Plugin 8+ and JDK17+


# 0.2.2

##### Fix
* Add mustRunAfter assemble and bundle tasks without side effect on configuration cache 


# 0.2.1

##### Fix
* ReleaseNotes doesn't work #1


# 0.2.0

First released version. Support:
* Publish APK in Rustore and submit it on all users after got store approve
