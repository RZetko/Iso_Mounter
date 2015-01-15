package info.ipsec.zdenko.isomounter;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;

import java.util.concurrent.TimeoutException;

/**
 * Created by Zdenko on 6.5.2014.
 */
public class Util {
    public static int cmd(String cmd)
    {
        CommandCapture command = new CommandCapture(0, cmd);
        try {
            Command c = RootTools.getShell(true).add(command);
            c.waitForFinish();
            return c.exitCode();
        } catch (java.io.IOException e) {
            return -1;
        } catch (TimeoutException e) {
            return -1;
        } catch (RootDeniedException e) {
            return -1;
        } catch (InterruptedException e) {
            return -1;
        }
    }

}
