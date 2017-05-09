# vSphere-vApp Cloner and Configuration updater
### 1. Details
Utility Class to Clone vAPP from source host to destination host and update vApp configuration for StartupDelay, StopAction.
Performs/illustrates the following operations,
- Clone the vAPP on to a destination host
- Update the setting of cloned vApp
  -- Set VM startup delay to 0 : allow vApp to powerOn VMs simultaneously
  -- Set VM StopAction to 'powerOff' : allow vApp to powerOff VMs
- Power on vApp
- Power off vApp
- Destroy vApp

### 2. How to run the Utility?
##### Run from Dev IDE

 * Import files under the src/vappcloner/ folder into your IDE.
 * Required libraries are embedded within Runnable-Jar/vAppCloneUpdater.jar, extract & import the libraries into the project.
 *  Run the utility from 'RunApp' program by providing arguments like:  
 _--vsphereip 192.168.1.1 --username adminUser --password dummyPasswd --srcvapp SrcDummyvApp_

##### Run from Pre-built Jars
 * Copy/Download the vAppCloneUpdater.jar from Runnable-jar folder (from the uploaded file) and unzip on to local drive folder say c:\vAppCloner
 * Open a command prompt and cd to the folder, lets say cd vAppCloner
 * Run a command like shown below to see various usage commands:  
 _C:\vAppCloner>java -jar vAppCloneUpdater.jar --help_
 
### 3. Sample output
```
Logging into vSphere : 192.168.1.1, with provided credentials
Succesfully logged into vSphere: 192.168.1.1
Succesfully logged into VC: 192.168.1.1
Search for specified vApp in inventory...
Found vApp: SrcDummyvApp in inventory
Found Source Host: ESXi1Host
Retrieve Hosts list from inventory ...
Found more than one host in inventory, choosing target host for vApp Clone
vAPP Clone operation is about to start ...
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Host : ESXi2Host
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Monitor vApp: SrcDummyvApp20170509-155141-476ESXi2Host Clone task ...
[SrcDummyvApp20170509-155141-476ESXi2Host-Clone task] Task is still running
[SrcDummyvApp20170509-155141-476ESXi2Host-Clone task] Task Completed
vApp: SrcDummyvApp20170509-155141-476ESXi2Host creation suceeded
Update vApp: SrcDummyvApp20170509-155141-476ESXi2Host configuration - StartupDelay & StopAction...
Successfully updated vApp: SrcDummyvApp20170509-155141-476ESXi2Host with StartupDelay & StopAction configuration
Poweron vApp: SrcDummyvApp20170509-155141-476ESXi2Host
[SrcDummyvApp20170509-155141-476ESXi2Host-PowerOn vApp] Task is still running
[SrcDummyvApp20170509-155141-476ESXi2Host-PowerOn vApp] Task Completed
vAPP: SrcDummyvApp20170509-155141-476ESXi2Host has been poweredOn successfully
Begin cleanup tasks ...
Power off vApp: SrcDummyvApp20170509-155141-476ESXi2Host
[SrcDummyvApp20170509-155141-476ESXi2Host-PowerOff vApp] Task is still running
[SrcDummyvApp20170509-155141-476ESXi2Host-PowerOff vApp] Task Completed
vAPP: SrcDummyvApp20170509-155141-476ESXi2Host has been poweredOff successfully
Destory the vAPP
[SrcDummyvApp20170509-155141-476ESXi2Host-Destroy vApp] Task is still running
[SrcDummyvApp20170509-155141-476ESXi2Host-Destroy vApp] Task Completed
vApp: SrcDummyvApp20170509-155141-476ESXi2Host destroyed successfully
```

