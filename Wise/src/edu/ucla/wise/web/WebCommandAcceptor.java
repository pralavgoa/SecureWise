package edu.ucla.wise.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.shared.web.WebCommandUtil;

/**
 * Accepts commands using HTTP GET to perform specific functions such as
 * reloading study space parameters. These commands are ones that are not
 * specific to individual study spaces. Eg. /WebCommand?command={encoded_string}
 */
@WebServlet("/WebCommand")
public class WebCommandAcceptor extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(WebCommandAcceptor.class);

    public static final String COMMAND = "command";

    public static enum Command {
        RELOAD_STUDY_SPACES
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {

        PrintWriter out = res.getWriter();
        String encodedEncryptedCommandString = req.getParameter(COMMAND);

        if (Strings.isNullOrEmpty(encodedEncryptedCommandString)) {
            out.write("An encoded command is required. eg. /WebCommand?command={encoded_string}");
            return;
        }

        String encryptionKey = WISEApplication.getInstance().getWiseProperties().getStudySpaceWizardPassword();

        if (Strings.isNullOrEmpty(encryptionKey)) {
            String errorMessage = "The password for study space wizard is not provided in the configuration file";
            out.write(errorMessage);
            LOGGER.error(errorMessage);
        }

        String commandString = WebCommandUtil.getDecodedDecrypted(encodedEncryptedCommandString, encryptionKey);

        LOGGER.debug("commandString: '" + commandString + "'");

        Command command = Command.valueOf(commandString);

        switch (command) {
        case RELOAD_STUDY_SPACES:
            WISEApplication.getInstance().reloadStudySpaceParametersProvider();
            break;
        default:
            out.write("The functionality to execute '" + command + "' is not implemented");
        }

    }
}
