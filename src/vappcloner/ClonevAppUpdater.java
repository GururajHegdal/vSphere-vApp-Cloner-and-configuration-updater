/**
 * Utility Class to Clone vAPP from source host to destination host in inventory, performing/illustrating...
 * - Clone the vAPP on to a destination host
 * - Update the setting of cloned vApp
 * -- Set VM startup delay to 0 : allow vApp to powerOn VMs simultaneously
 * -- Set VM StopAction to 'powerOff' : allow vApp to powerOff VMs
 * - Power on vApp
 * - Power off vApp
 * - Destroy vApp
 *
 * Copyright (c) 2017
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * @author Gururaja Hegdal (ghegdal@vmware.com)
 * @version 1.0
 *
 *          The above copyright notice and this permission notice shall be
 *          included in all copies or substantial portions of the Software.
 *
 *          THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *          EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *          OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *          NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *          HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *          WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *          FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *          OTHER DEALINGS IN THE SOFTWARE.
 */

package vappcloner;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.TaskInfoState;
import com.vmware.vim25.VAppCloneSpec;
import com.vmware.vim25.VAppConfigSpec;
import com.vmware.vim25.VAppEntityConfigInfo;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualApp;
import com.vmware.vim25.mo.VirtualMachine;

public class ClonevAppUpdater {
    // VC inventory related objects
    private static final String DC_MOR_TYPE = "Datacenter";
    private static final String RESPOOL_MOR_TYPE = "ResourcePool";
    private static final String HOST_MOR_TYPE = "HostSystem";
    private static final String VIRTUAL_APP_MOR_TYPE = "VirtualApp";
    private static final String VAPP_STOP_ACTION = "powerOff";

    private String vsphereIp;
    private String userName;
    private String password;
    private String srcvappName;
    private String url;
    private ServiceInstance si;
    private ManagedObjectReference destHostsMor;
    private VirtualApp srcvAppObject;

    /**
     * Constructors
     */
    public ClonevAppUpdater(String[] cmdProps) {
        makeProperties(cmdProps);
    }

    public ClonevAppUpdater() {
        // TODO Auto-generated constructor stub
    }

    /**
     * Read properties from command line arguments
     */
    private void
    makeProperties(String[] cmdProps)
    {
        // get the property value and print it out
        System.out.println("Reading vSphere IP and Credentials information from command line arguments");
        System.out.println("-------------------------------------------------------------------");

        for (int i = 0; i < cmdProps.length; i++) {
            if (cmdProps[i].equals("--vsphereip")) {
                vsphereIp = cmdProps[i + 1];
                System.out.println("vSphere IP:" + vsphereIp);
            } else if (cmdProps[i].equals("--username")) {
                userName = cmdProps[i + 1];
                System.out.println("VC Username:" + userName);
            } else if (cmdProps[i].equals("--password")) {
                password = cmdProps[i + 1];
                System.out.println("VC password: ******");
            } else if (cmdProps[i].equals("--srcvapp")) {
                srcvappName = cmdProps[i + 1];
                System.out.println("Source vApp name:" + srcvappName);
            }
        }
        System.out.println("-------------------------------------------------------------------\n");
    }

    /**
     * Validate property values
     */
    boolean
    validateProperties()
    {
        boolean val = false;
        if (vsphereIp != null) {
            url = "https://" + vsphereIp + "/sdk";

            // Login to provided server IP to determine if we are running against single ESXi
            try {
                System.out.println("Logging into vSphere : " + vsphereIp + ", with provided credentials");
                si = loginTovSphere(url);

                if (si != null) {
                    System.out.println("Succesfully logged into vSphere: " + vsphereIp);
                    val = true;
                } else {
                    System.err.println(
                        "Service Instance object for vSphere:" + vsphereIp + " is null, probably we failed to login");
                    printFailedLoginReasons();
                }
            } catch (Exception e) {
                System.err.println(
                    "Caught an exception, while logging into vSphere :" + vsphereIp + " with provided credentials");
                printFailedLoginReasons();
            }
        }
        return val;
    }

    /**
     * Method prints out possible reasons for failed login
     */
    private void
    printFailedLoginReasons()
    {
        System.err.println(
            "Possible reasons:\n1. Provided username/password credentials are incorrect\n"
                + "2. If username/password or other fields contain special characters, surround them with double "
                + "quotes and for non-windows environment with single quotes (Refer readme doc for more information)\n"
                + "3. vCenter Server/ESXi server might not be reachable");
    }

    /**
     * Login method to VC
     */
    private ServiceInstance
    loginTovSphere(String url)
    {
        try {
            si = new ServiceInstance(new URL(url), userName, password, true);
        } catch (Exception e) {
            System.out.println("Caught exception while logging into vSphere server");
            e.printStackTrace();
        }
        return si;
    }

    /**
     * vApp Cloning handler main function
     */
    public void
    vAppCloningHandler() throws Exception
    {
        // login to vcva
        si = loginTovSphere(url);
        assert (si != null);
        System.out.println("Succesfully logged into VC: " + vsphereIp);

        System.out.println("Search for specified vApp in inventory...");
        ManagedEntity orivAppMe = retrievevApp(srcvappName);

        if (orivAppMe != null) {
            System.out.println("Found vApp: " + srcvappName + " in inventory");
            ManagedObjectReference orivAppMor = orivAppMe.getMOR();
            srcvAppObject = new VirtualApp(si.getServerConnection(), orivAppMor);
            VirtualMachine[] vms = srcvAppObject.getVMs();

            if (vms.length > 0) {
                ManagedObjectReference srcHostMor = vms[0].getRuntime().getHost();
                HostSystem hs = new HostSystem(si.getServerConnection(), srcHostMor);
                String srcHostName = hs.getName();
                System.out.println("Found Source Host: " + srcHostName);

                System.out.println("Retrieve Hosts list from inventory ...");
                ManagedEntity[] allHosts = retrieveHosts();

                if (allHosts.length > 1) {
                    System.out.println("Found more than one host in inventory, choosing target host for vApp Clone");

                    for (ManagedEntity tempHostMe : allHosts) {
                        if (!tempHostMe.getName().equals(srcHostName)) {
                            destHostsMor = tempHostMe.getMOR();
                        }
                    }
                } else {
                    System.out.println("There is only one host in the inventory, using the same as target host");
                    destHostsMor = srcHostMor;
                }
            } else {
                System.err.println("Could not find any VMs in vApp: " + srcvappName);
            }
        } else {
            System.err.println("Could not find vApp: " + srcvappName + " in inventory");
        }

        // Clone vApp and update settings; before powering it on. finally destroy it
        vAppClonerWorkflow(destHostsMor);
    }

    /**
     * vApp cloner method
     */
    private void
    vAppClonerWorkflow(ManagedObjectReference hostMor)
    {
        try {
            HostSystem ihs = new HostSystem(si.getServerConnection(), hostMor);
            String hostName = ihs.getName();
            System.out.println("vAPP Clone operation is about to start ...");
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            System.out.println("Host : " + hostName);
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

            VAppCloneSpec vappCloneSpec = new VAppCloneSpec();
            vappCloneSpec.setHost(hostMor);

            Datastore targetDs = null;
            HostSystem tempIhs = new HostSystem(si.getServerConnection(), hostMor);

            for (Datastore tempDs : tempIhs.getDatastores()) {

                if (tempDs.getSummary().isAccessible()) {
                    targetDs = tempDs;
                    break;
                }
            }
            vappCloneSpec.setLocation(targetDs.getMOR());

            ResourcePool targetResPool = null;
            ComputeResource hostResource = new ComputeResource(si.getServerConnection(), hostMor);
            ManagedEntity[] allRespools = new InventoryNavigator(si.getRootFolder())
                .searchManagedEntities(RESPOOL_MOR_TYPE);

            for (int i = 0; i < allRespools.length; i++) {
                ResourcePool tempResPool = (ResourcePool) allRespools[i];

                if (tempResPool.getOwner().getName().equals(hostResource.getName())) {
                    targetResPool = tempResPool;
                    break;
                }
            }

            // Get VM Folder reference
            ManagedEntity tempMgdEntityObj = tempIhs.getParent();

            while (!tempMgdEntityObj.getMOR().getType().equals(DC_MOR_TYPE)) {
                tempMgdEntityObj = tempMgdEntityObj.getParent();
            }

            Folder vmFolder = ((Datacenter) tempMgdEntityObj).getVmFolder();
            vappCloneSpec.setVmFolder(vmFolder.getMOR());

            // Clone vApp
            String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(Calendar.getInstance().getTime());
            String newvAppName = srcvappName + timeStamp + hostName;
            Task taskWhole = srcvAppObject.cloneVApp_Task(newvAppName, targetResPool.getMOR(), vappCloneSpec);

            VirtualApp newvAppObject = null;
            boolean settingsUpdated = true;
            System.out.println("Monitor vApp: " + newvAppName + " Clone task ...");

            if (taskTracker(taskWhole, newvAppName, "Clone task")) {
                ManagedEntity newvAppMe = retrievevApp(newvAppName);

                if (newvAppMe != null) {
                    System.out.println("vApp: " + newvAppName + " creation suceeded");
                    newvAppObject = new VirtualApp(si.getServerConnection(), newvAppMe.getMOR());

                    System.out.println("Update vApp: " + newvAppName + " configuration - StartupDelay & StopAction...");
                    // Update vApp configuration to allow us to poweron multiple VMs in a threaded way
                    VAppEntityConfigInfo[] entityConfigInfo = newvAppObject.getVAppConfig().getEntityConfig();

                    for (int i = 0; i < entityConfigInfo.length; i++) {
                        /*
                         * Set start delay to 0 - allows us to see that all VMs in vApp are poweredon
                         * simultaneously
                         */
                        entityConfigInfo[i].setStartDelay(0);

                        /*
                         * Set Shutdown action as poweroff - allows us to see that all VMs in vApp are
                         * poweredoff simultaneously
                         */
                        entityConfigInfo[i].setStopAction(VAPP_STOP_ACTION);
                    }

                    VAppConfigSpec spec = new VAppConfigSpec();
                    // Set the updated configuration information in the update spec
                    spec.setEntityConfig(entityConfigInfo);

                    // Update the vApp with modified spec
                    newvAppObject.updateVAppConfig(spec);

                    // Validate that our changes are indeed applied on the vApp
                    entityConfigInfo = newvAppObject.getVAppConfig().getEntityConfig();

                    for (int i = 0; i < entityConfigInfo.length; i++) {
                        if (!(entityConfigInfo[i].getStartDelay() == 0
                            && entityConfigInfo[i].getStopAction().equals(VAPP_STOP_ACTION))) {
                            System.err.println("Earlier updated settings on vApp: " + newvAppName + " did not persist");
                            settingsUpdated = false;
                            break;
                        }
                    }

                    if (settingsUpdated) {
                        System.out.println(
                            "Successfully updated vApp: " + newvAppName
                                + " with StartupDelay & StopAction configuration");
                        System.out.println("Poweron vApp: " + newvAppName);
                        Task vAppPowerOnTask = newvAppObject.powerOnVApp_Task();

                        // Monitor all VMs poweron task
                        if (taskTracker(vAppPowerOnTask, newvAppName, "PowerOn vApp")) {
                            System.out.println("vAPP: " + newvAppName + " has been poweredOn successfully");
                        } else {
                            System.err.println("vApp: " + newvAppName + " could NOT be powered on");
                        }
                    }

                } else {
                    System.err.println("Could not find cloned vApp: " + newvAppName + " in inventory");
                }
            } else {
                System.err.println("vApp: " + newvAppName + " creation task failed");
            }

            // Clean up the cloned vApp
            if (newvAppObject != null) {

                System.out.println("Begin cleanup tasks ...");
                Thread.sleep(1000 * 10);

                if (settingsUpdated) {
                    System.out.println("Power off vApp: " + newvAppName);
                    Task vAppPowerOffTask = newvAppObject.powerOffVApp_Task(true);

                    // Monitor all VMs poweroff task
                    if (taskTracker(vAppPowerOffTask, newvAppName, "PowerOff vApp")) {
                        System.out.println("vAPP: " + newvAppName + " has been poweredOff successfully");
                    } else {
                        System.err.println("vApp: " + newvAppName + " could NOT be powered off");
                    }
                }

                System.out.println("Destory the vAPP");
                Thread.sleep(1000 * 10);

                if (taskTracker(newvAppObject.destroy_Task(), newvAppName, "Destroy vApp")) {
                    System.out.println("vApp: " + newvAppName + " destroyed successfully");
                } else {
                    System.err.println("vApp: " + newvAppName + " could not be destroyed");
                }
            }

        } catch (Exception e) {
            System.err.println("[Error] Caught exception while Cloning vApp");
        }
    }

    /**
     * Monitor Task progress and return final state
     */
    private boolean
    taskTracker(Task taskMor, String vappName, String operation) throws Exception
    {
        boolean isTaskSuccess = false;

        TaskInfoState taskState = taskMor.getTaskInfo().getState();

        while (!(taskState.equals(TaskInfoState.success))) {

            if ((taskState.equals(TaskInfoState.error))) {
                System.err.println("[" + vappName + "-" + operation + "] Task errored out");
                break;
            } else {
                System.out.println("[" + vappName + "-" + operation + "] Task is still running");
                Thread.sleep(2000);
            }
            taskState = taskMor.getTaskInfo().getState();
        }

        if (taskState.equals(TaskInfoState.success)) {
            System.out.println("[" + vappName + "-" + operation + "] Task Completed");
            isTaskSuccess = true;
        }

        return isTaskSuccess;
    }

    /**
     * All hosts
     */
    private ManagedEntity[]
    retrieveHosts()
    {
        InventoryNavigator navigator = new InventoryNavigator(si.getRootFolder());
        ManagedEntity[] hosts = null;

        try {
            hosts = navigator.searchManagedEntities(HOST_MOR_TYPE);
        } catch (Exception e) {
            System.err.println("[Error] Unable to retrieve Hosts from inventory");
            e.printStackTrace();
        }
        return hosts;
    }

    /**
     * Search for specified vAPP and return its ManagedEntity
     */
    private ManagedEntity
    retrievevApp(String vAppName)
    {
        InventoryNavigator navigator = new InventoryNavigator(si.getRootFolder());
        ManagedEntity vAppMe = null;

        try {
            ManagedEntity[] vAppMeArray = navigator.searchManagedEntities(VIRTUAL_APP_MOR_TYPE);

            for (ManagedEntity tempvApp : vAppMeArray) {
                if (tempvApp.getName().equals(vAppName)) {
                    vAppMe = tempvApp;
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("[Error] Unable to retreive specified vApp from inventory");
            e.printStackTrace();
        }
        return vAppMe;
    }

} // End of main class
