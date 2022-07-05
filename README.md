# Mitek Auth Tree Node

## Mitek

## Installation

The Mitek-Auth tree nodes are packaged as a jar file and the latest release can be download [here](https://github.com/Enzoic/forgerock/releases/latest).
 Once downloaded, copy the jar file to the ../web-container/webapps/openam/WEB-INF/lib
 
## Steps

1) Configure Maven to be able to access the OpenAM repositories

2) Setup a Mitek Maven Project and run mvn package command.

3) The project will generate a .jar file containing our custom nodes. i.e. Mitek-ForgeRock-Integration-1.0.jar

5) Copy the Mitek-ForgeRock-Integration-1.0.jar file to the WEB-INF/lib/ folder where AM is deployed

6) Restart the AM for the new plug-in to become available.


## Mitek Auth Tree Configuration

Below are the nodes that will be available after deploying the jar file:

### Capture Back
```js
This node will capture back image of document.
```

### Capture Front
```js
This node will capture front image of document.
```
 
### Consent
```js
This node will display consent message to user.
```

### Retry Limit Decision
```js
Applies retry if verfication response code is 500. This is a built in ForgeRock Node 
```

Configuration is:
```js
* Retry Limit : The number of times to allow a retry.
```
![retry](https://user-images.githubusercontent.com/20396535/57918264-0849a000-78b4-11e9-905f-78ef61b88986.PNG)


### Mitek Configuration
```js
This node will get configuration detail while set up nodes.
```

Configuration is:
```js
* Api url : URL to connect with Mitek SDK

* Api client id : Client id for http request authentication.

* Api client secret : Client secret for http request authentication.

* Scope : Scope for http request.
```

<img width="263" alt="Screenshot 2022-07-05 at 5 09 34 PM" src="https://user-images.githubusercontent.com/106667867/177319545-5de3be69-504c-4a41-8533-29ffe9dbb780.png">


### Passport node
```js
This node will capture image of passport.
```

### Review
```js
This node will connect to Mitek SDK and execute image verification. This node also provide option to user for retake image again.
```

### Selfie node
```js
This node will capture image of selfie.
```

### Verification Failure
```js
This node will redirect user to failure page and display failure message.
```

### Verification Option
```js
This node will provide option to user to select document on which they want to do identity verification.
```

### Verification Outcome
```js
This node will redirect user to failure/success or retry page based on verification result.
```

### Verification Retry
```js
This node will redirect user to retry page and display retry message.
```

### Verification Success
```js
This node will redirect user to success page and display success message.
```




## Configure the trees as follows

### Document Verification :
![DocumentVerification](https://user-images.githubusercontent.com/106667867/177316229-95287803-94c6-4861-a9f3-9a6adfed1abb.png)


### Passport-Selfie Verification :
![Passport-SelfieVerification](https://user-images.githubusercontent.com/106667867/177316813-bcc2e3c5-089e-4e5d-8305-a484127b67c5.png)


### Passport Only Verification :
![PassportVerification](https://user-images.githubusercontent.com/106667867/177317327-930e5eaa-bc2f-4ece-b23c-d4ace2f5f99e.png)


### Document-Selfie Verification : 
![doc-selfieVerification](https://user-images.githubusercontent.com/106667867/177319805-69617f82-960d-49a1-be3b-bcaa028c760e.png)


## Set Logging Level

* User can set log level in forgerock instance, To set user need to follow this path:
```js
DEPLOYMENT-->SERVERS-->LocalInstance-->Debugging
```

## Configure the trees as follows
```js
* Navigate to **Realm** > **Authentication** > **Trees** > **Create Tree**
```

