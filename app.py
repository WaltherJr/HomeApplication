from flask import Flask, jsonify, request, make_response, render_template
from subprocess import PIPE, run, Popen
import re
import time

'''
LÄGG IN SOM STARTUP-SCRIPT: konsole --noclose -e /bin/bash /home/radxa/Desktop/hdmi-cec-server/run_server.sh
export FLASK_ENV=development && python3 -m flask run -p 8000 -h 0.0.0.0
TV:n är icke-konfigurerad om man får "Transmit from Unregistered to TV (15 to 0):". Man ska få "Transmit from Specific to TV (14 to 0):
STANDBY (0x36)
       Sequence: 76 Tx Timestamp: 479.949s
'''
'''
Kör man "cec-ctl -d/dev/cec0 --show-topology" ska man få detta om TV:n är konfigurerad:
    Topology:

    0.0.0.0: TV
        1.0.0.0: Specific
'''

app = Flask(__name__)
FIRST_HDMI_CHANNEL = 1
LAST_HDMI_CHANNEL = 3
INVALID_REQUEST_PARAMETER_VALUE = "Invalid value \"{parameterValue}\" for request parameter \"{parameterName}\""
MANDATORY_REQUEST_PARAMETER_IS_MISSING = "Mandatory request parameter \"{parameterName}\" is missing"

class ValidationError(Exception):
    pass

def issue_hdmi_cec_command(command):
    result1 = run(command, stdout = PIPE, stderr = PIPE, universal_newlines = True, shell=True)
    result1_stdout = result1.stdout.strip()
    result1_stderr = result1.stderr.strip()
    not_configured_adapter_message = re.search("Adapter is unconfigured, please configure it first.", result1_stderr)
    not_visible_adapter_message = re.search("Sequence: .+ Timestamp: .+$", result1_stdout)
    adapter_error_message = None
    # print("STDOUT:=====================\n\n" + result1_stdout + "\n\n=====================")
    # print("WAS SUCCESSFUL COMMAND: " + (" TRUE" if match2 else " FALSE"))
    if not_configured_adapter_message:
        adapter_error_message = "TV adapter is unconfigured, configuring..."
    elif not not_visible_adapter_message:
        adapter_error_message = "TV adapter is not visible, fixing..."
    if not_configured_adapter_message or not not_visible_adapter_message:
        # print("SETTING ERROR TO: " + adapter_error_message)
        configure_tv_result = configure_hdmi_cec()
        print("TV adapter is now configured/visible, running command one more time")
        time.sleep(1)
        result2 = run(command, stdout = PIPE, stderr = PIPE, universal_newlines = True, shell=True)
        return [{"command": command, "stdout": result1.stdout, "stderr": result1.stderr}, configure_tv_result, {"command": command, "stdout": result2.stdout, "stderr": result2.stderr}]
    return {"command": command, "stdout": result1.stdout, "stderr": result1.stderr}

def configure_hdmi_cec():
    command = "cec-ctl -d/dev/cec0 --tv -S"
    result = run(command, stdout = PIPE, stderr = PIPE, universal_newlines = True, shell=True)
    return {"command": command, "stdout": result.stdout, "stderr": result.stderr}

def create_response(statusCode, responseJSON):
    response = make_response(jsonify(responseJSON))
    response.status_code = statusCode
    response.mimetype = "application/json"
    return response

def create_error_json_response(statusCode, response):
    return create_response(statusCode, {"responseType": "ERROR", "response": response})

def create_success_json_response(statusCode, response):
    return create_response(statusCode, {"responseType": "SUCCESS","response": response})

@app.route("/", methods = ["GET"])
def hello():
    return "Hello from Radxa Rock server!"

@app.route("/hdmi-cec/command-result", methods = ["GET"])
def get_hdmi_cec_command_result():
    try:
        commandString = validate_parameter(request.args.get("command"), "command", "^.+$", "{parameter}", True)
        result = run(commandString, stdout = PIPE, stderr = PIPE, universal_newlines = True, shell=True)
        return create_success_json_response(200, {"command": commandString, "stdout": result.stdout, "stderr": result.stderr})
    except ValidationError as e:
        return create_error_json_response(400, str(e))
    except Exception as e:
        return create_error_json_response(500, str(e))

@app.route("/hdmi-cec/configuration", methods = ["GET"])
def get_hdmi_cec_configuration():
    try:
        result = configure_hdmi_cec()
        return create_success_json_response(200, result)
    except Exception as e:
        return create_error_json_response(500, str(e))

def validate_to_channel_parameter(request):
    return validate_parameter(request.args.get("to"), "to", "^[0-9]$", "--to {parameter}", False)

@app.route("/hdmi-cec/topology", methods = ["GET"])
def get_hdmi_cec_topology():
    try:
        toChannelString = validate_to_channel_parameter(request)
        command = "cec-ctl" + (f" {toChannelString}" if toChannelString else "") + " -d/dev/cec0 --show-topology"
        result = run(command, stdout = PIPE, stderr = PIPE, universal_newlines = True, shell=True)
        return create_success_json_response(200, {"command": command, "stdout": result.stdout, "stderr": result.stderr})

    except ValidationError as e:
        return create_error_json_response(400, str(e))
    except Exception as e:
        return create_error_json_response(500, str(e))

@app.route("/current-hdmi-channel", methods = ["GET"])
def get_current_hdmi_channel():
    try:
        result = issue_hdmi_cec_command("cec-ctl" + (f" {toChannelString}" if toChannelString else "") +  f" --active-source phys-addr={channelString}.0.0.0")
        return create_success_json_response(200, result)

    except ValidationError as e:
        return create_error_json_response(400, str(e))
    except Exception as e:
        return create_error_json_response(500, str(e))

@app.route("/current-hdmi-channel", methods = ["PUT"])
def switch_hdmi_channel():
    try:
        requestBody = request.json
        channelString = validate_parameter(requestBody.get("channel"), "channel", "^[{min}-{max}]$".format(min = FIRST_HDMI_CHANNEL, max = LAST_HDMI_CHANNEL), "{parameter}", True)
        toChannelString = validate_to_channel_parameter(request)
        result = issue_hdmi_cec_command("cec-ctl" + (f" {toChannelString}" if toChannelString else "") +  f" --active-source phys-addr={channelString}.0.0.0")
        return create_success_json_response(200, result)

    except ValidationError as e:
        return create_error_json_response(400, str(e))
    except Exception as e:
        return create_error_json_response(500, str(e))


@app.route("/tv/standbystate", methods = ["PUT"])
def set_tv_standby_state():
    try:
        requestBody = request.json
        standByStateString = validate_parameter(requestBody.get("standbyState"), "standbyState", "^(on|off)$", "{parameter}", True)
        toChannelString = validate_to_channel_parameter(request)
        command = "cec-ctl" + (f" {toChannelString}" if toChannelString else "") + " -d/dev/cec0"
        if standByStateString == "off":
            command += " --standby"
        elif standByStateString == "on":
            command += " --image-view-on"

        result = issue_hdmi_cec_command(command)
        return create_success_json_response(200, result)

    except ValidationError as e:
        return create_error_json_response(400, str(e))
    except Exception as e:
        return create_error_json_response(500, str(e))


def validate_parameter(parameterValue, parameterName, validParameterRegex, returnValueStringTemplate, mandatory = False):
    if (parameterValue):
        parameterValueStr = str(parameterValue)
        if (not re.match(validParameterRegex, parameterValueStr)):
            raise ValidationError(INVALID_REQUEST_PARAMETER_VALUE.format(parameterValue = parameterValueStr, parameterName = parameterName))
        else:
            return returnValueStringTemplate.format(parameter = parameterValueStr)
    elif (mandatory):
        raise ValidationError(MANDATORY_REQUEST_PARAMETER_IS_MISSING.format(parameterName = parameterName))
