/**
 * Utility Class to Clone vAPP from source host to destination host in inventory, performing/illustrating...
 * - Clone the vAPP on to a destination host
 * - Update the setting of cloned vApp
 *   -- Set VM startup delay to 0 : allow vApp to powerOn VMs simultaneously
 *   -- Set VM StopAction to 'powerOff' : allow vApp to powerOff VMs
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

// Entry point into the vApp cloner tool
public class RunApp
{
    /**
     * Usage method - how to use/invoke the script, reveals the options supported through this script
     */
    public static void usagevAppCloneScript()
    {
        System.out.println(
            "Usage: java -jar vAppCloneUpdater.jar --vsphereip <VC IP> --username <uname> --password <pwd> --srcvapp <SourcevAppName>");
        System.out.println(
            "\"java -jar vAppCloneUpdater.jar --vsphereip 10.4.5.6 --username admin --password dummyPwd --srcvapp MyvApp\"");
    }

    /**
     * Main entry point
     *
     * @throws Exception
     */
    public static void main(String[] args)
    {

        System.out.println(
            "######################### vApp Cloner Script execution STARTED #########################");

        try {
            // Read command line arguments
            if (args.length > 0 && args.length >= 8) {
                ClonevAppUpdater vAppClassObj = new ClonevAppUpdater(args);
                if (vAppClassObj.validateProperties()) {
                    vAppClassObj.vAppCloningHandler();
                } else {
                    usagevAppCloneScript();
                }
            } else {
                usagevAppCloneScript();
            }

            Thread.sleep(1000 * 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(
            "######################### vApp Cloner Script execution completed #########################");
    }
}