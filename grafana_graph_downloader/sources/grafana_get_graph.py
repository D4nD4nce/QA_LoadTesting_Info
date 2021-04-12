#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# ----------------------------------------------------------------------------------------------------------------------
import argparse
import configparser
import datetime
import http.cookiejar
import os
import re
import ssl
import sys
import traceback
import urllib.error
import urllib.error
import urllib.parse
import urllib.parse
import urllib.request
import urllib.request
import json
import csv
import itertools

# noinspection PyProtectedMember
ssl._create_default_https_context = ssl._create_unverified_context

_SUPPORTED_GRAFANA_VERSIONS = ("3.0", "3.2", "3.4", "4.0")

_CONST_CONFIG_GRAFANA_VERSION = 'grafana_version'
_CONST_CONFIG_IMG_WIDTH = 'img_width'
_CONST_CONFIG_ING_HEIGHT = 'img_height'
_CONST_CONFIG_IMG_LEGEND = 'img_legend'
_CONST_CONFIG_IMG_DIRECTORY = 'img_directory'
_CONST_CONFIG_GRAFANA_URL = 'grafana_url'
_CONST_CONFIG_BEARER_KEY = 'bearer_key'
_CONST_CONFIG_DASH_ID = 'dash_id'
_CONST_CONFIG_ORG_ID = 'org_id'
_CONST_CONFIG_GRAPHIDS = 'graphids'
_CONST_CONFIG_VAR_FILES = 'var_files'
_CONST_CONFIG_TIME_FROM = 'time_from'
_CONST_CONFIG_TIME_TILL = 'time_till'
_CONST_CONFIG_IMG_NAME = 'img_name'

_CONST_ROW_ID = 'row_id'
_CONST_PANELS_UNDER_ROW = 'panels_under_row'

_CONST_VAR_NAME = 'var_name'
_CONST_VAR_VALUE = 'var_value'


def main():
    # __________________________________________________________________________
    # command-line options, arguments
    try:
        parser = argparse.ArgumentParser(description='Grafana Get Graph - '
                                                     'utility for downloading graphs from Grafana Frontend')
        parser.add_argument("task", action='store', default=None, nargs='?', metavar='<TASK>', help="specified task")
        parser.add_argument('--test', action='store_true', default=False, help="test mode")
        args = parser.parse_args()
    except SystemExit:
        return False
    # __________________________________________________________________________
    # read configuration file
    try:
        self_dir = os.path.abspath(os.path.dirname(sys.argv[0]))
        config_ini = configparser.ConfigParser()
        config_ini.read(os.path.join(self_dir, 'config.ini'))
    except Exception as err:
        print("[!!] Unexpected Exception: {}\n{}".format(err, "".join(traceback.format_exc())), flush=True)
        return False
    # __________________________________________________________________________
    if not [x for x in config_ini.sections() if x != 'default']:
        print("[..] Nothing to do", flush=True)
        return False
    # ==================================================================================================================
    # ==================================================================================================================
    # Start of the work cycle
    # ==================================================================================================================
    for task in [x for x in config_ini.sections() if x != 'default']:
        if args.task and task != args.task:
            continue
        print("[--] Starting: {}".format(task), flush=True)
        config_task = {
            _CONST_CONFIG_GRAFANA_VERSION: "4.0",
            _CONST_CONFIG_IMG_WIDTH: None,
            _CONST_CONFIG_ING_HEIGHT: None,
            _CONST_CONFIG_IMG_LEGEND: True,
            _CONST_CONFIG_IMG_DIRECTORY: None,
            _CONST_CONFIG_GRAFANA_URL: None,
            _CONST_CONFIG_BEARER_KEY: None,
            _CONST_CONFIG_DASH_ID: None,
            _CONST_CONFIG_ORG_ID: None,
            _CONST_CONFIG_GRAPHIDS: [],
            _CONST_CONFIG_VAR_FILES: [],
            _CONST_CONFIG_TIME_FROM: None,
            _CONST_CONFIG_TIME_TILL: None,
            _CONST_CONFIG_IMG_NAME: "$ID"
        }
        # config default
        for x in config_task:
            try:
                config_task[x] = config_ini['default'][x]  # <'str'>
            except KeyError:
                pass
            except Exception as err:
                print("[!!] Unexpected Exception: {}\n{}".format(err, "".join(traceback.format_exc())), flush=True)
                return False
        # config task
        for x in config_task:
            try:
                config_task[x] = config_ini[task][x]  # <'str'>
            except KeyError:
                pass
            except Exception as err:
                print("[!!] Unexpected Exception: {}\n{}".format(err, "".join(traceback.format_exc())), flush=True)
                return False

        # ______________________________________________________________________
        # grafana_version
        if config_task[_CONST_CONFIG_GRAFANA_VERSION] not in _SUPPORTED_GRAFANA_VERSIONS:
            print("[EE] Unsupported version: {}".format(config_task[_CONST_CONFIG_GRAFANA_VERSION]), flush=True)
            return False

        # graphids - optional
        if config_task[_CONST_CONFIG_GRAPHIDS]:
            config_task[_CONST_CONFIG_GRAPHIDS] = config_task[_CONST_CONFIG_GRAPHIDS].strip().split()
        else:
            print("[..] There are no arguments for graph IDs, all graphs from Dashboard will be downloaded", flush=True)

        # var_files - optional
        if config_task[_CONST_CONFIG_VAR_FILES]:
            config_task[_CONST_CONFIG_VAR_FILES] = config_task[_CONST_CONFIG_VAR_FILES].strip().split()
        else:
            print("[..] There are no var files in config, all graphs will be downloaded with no params", flush=True)

        # bearer_key
        if not config_task[_CONST_CONFIG_BEARER_KEY]:
            print("[EE] Invalid value for argument: {}".format(_CONST_CONFIG_BEARER_KEY), flush=True)
            return False

        # dash_id
        if not config_task[_CONST_CONFIG_DASH_ID]:
            print("[EE] Invalid value for argument: {}".format(_CONST_CONFIG_DASH_ID), flush=True)
            return False

        # org_id
        if not config_task[_CONST_CONFIG_ORG_ID]:
            print("[EE] Invalid value for argument: {}".format(_CONST_CONFIG_ORG_ID), flush=True)
            return False

        # img_name
        re_simple_str = re.compile(r"^([\w\-$]*)$")
        if not re_simple_str.search(config_task[_CONST_CONFIG_IMG_NAME]):
            print("[EE] Invalid value for argument: {}".format(_CONST_CONFIG_IMG_NAME), flush=True)
            return False

        # img_legend
        if isinstance(config_task[_CONST_CONFIG_IMG_LEGEND], str):
            if config_task[_CONST_CONFIG_IMG_LEGEND].lower() in ('0', 'false', 'off'):
                config_task[_CONST_CONFIG_IMG_LEGEND] = False
            elif config_task[_CONST_CONFIG_IMG_LEGEND].lower() in ('1', 'true', 'on'):
                config_task[_CONST_CONFIG_IMG_LEGEND] = True
            else:
                print("[EE] Invalid value for argument: {}".format(_CONST_CONFIG_IMG_LEGEND), flush=True)
                return False

        # img_directory
        if config_task[_CONST_CONFIG_IMG_DIRECTORY]:
            create_directories(config_task[_CONST_CONFIG_IMG_DIRECTORY])
        if not fs_check_access_dir('rw', config_task[_CONST_CONFIG_IMG_DIRECTORY]):
            return False

        # time_period
        try:
            time_from = datetime.datetime.strptime(config_task[_CONST_CONFIG_TIME_FROM], "%Y/%m/%d %H:%M:%S")
            time_till = datetime.datetime.strptime(config_task[_CONST_CONFIG_TIME_TILL], "%Y/%m/%d %H:%M:%S")
            time_period = int((time_till - time_from).total_seconds())
        except Exception as err:
            print("[!!] Unexpected Exception: {}\n{}".format(err, "".join(traceback.format_exc())), flush=True)
            return False

        # ______________________________________________________________________
        # Download dashboard json and parse panel's ids
        # graphs_array = []
        cj = http.cookiejar.CookieJar()
        opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))
        g_url = config_task[_CONST_CONFIG_GRAFANA_URL].rstrip('/')
        g_url += "/api/dashboards/uid/{}".format(config_task[_CONST_CONFIG_DASH_ID])
        opener.addheaders = [
            ('Authorization', str('Bearer ' + config_task[_CONST_CONFIG_BEARER_KEY])),
            ('Content-type', 'application/json'),
        ]
        print("[..] trying to get grafana dash json, sending: {}".format(g_url), flush=True)
        response = opener.open(g_url, timeout=1000)
        pars_json = json.loads(response.read())
        grafana_dash_name = get_last_url_elem(pars_json['meta']['url'])
        print("[..] grafana dash name {}".format(grafana_dash_name), flush=True)
        panels_array = pars_json['dashboard']['panels']
        structured_panels_array = get_structured_panels_array(panels_array, config_task[_CONST_CONFIG_GRAPHIDS])

        if not structured_panels_array:
            print("[EE] there are no panels found on the dashboard {}!".format(grafana_dash_name), flush=True)
            return False

        # ______________________________________________________________________
        # get csv vars (if exists)
        var_files_array = []
        combined_files_vars = []
        if config_task[_CONST_CONFIG_VAR_FILES]:
            for var_file in config_task[_CONST_CONFIG_VAR_FILES]:
                var_files_array.append(var_file)
            combined_files_vars = get_vars_from_files(var_files_array)

        # ______________________________________________________________________
        # Download all found graphs
        print("[..] Time period: '{}' - '{}' ({}s)".format(time_from, time_till, time_period), flush=True)

        # http://172.30.48.58:3000/render/d-solo/9gV-j1pZz/4it?orgId=1&from=1569241800000&to=1569243480000&panelId=2&width=1000&height=500&tz=Europe%2FMoscow
        # http://localhost:3000/render/d-solo/9gV-j1pZz/4it?panelId=26&orgId=1&from=1572354000000&to=1572357600000&var-_script=uc08&var-_transaction=uc08-01_Logon&width=1000&height=500&tz=Europe%2FMoscow
        # http://localhost:3000/render/d-solo/9gV-j1pZz/4it?orgId=1&from=1576740995892&to=1576762595892&var-_script=01&var-_transaction=uc01-01_Logon&panelId=38&width=1000&height=500&tz=Europe%2FMoscow

        for row_with_panel_stack in structured_panels_array:
            current_row_id = row_with_panel_stack[_CONST_ROW_ID]
            current_row_name = get_panel_name_from_json(current_row_id, panels_array)
            current_stack_panels_array = row_with_panel_stack[_CONST_PANELS_UNDER_ROW]

            img_file_path = config_task[_CONST_CONFIG_IMG_DIRECTORY]
            if current_row_id >= 0:
                img_file_path = get_proper_path(os.path.join(config_task[_CONST_CONFIG_IMG_DIRECTORY],
                                                             current_row_name))
            create_directories(img_file_path)

            for current_panel_id in current_stack_panels_array:
                current_panel_name = get_panel_name_from_json(current_panel_id, panels_array)

                if combined_files_vars:
                    for var_array in combined_files_vars:
                        if not get_and_save_panel_png(config_task, grafana_dash_name, current_panel_name,
                                                      current_panel_id, time_from, time_till, img_file_path, var_array):
                            continue
                else:
                    if not get_and_save_panel_png(config_task, grafana_dash_name, current_panel_name, current_panel_id,
                                           time_from, time_till, img_file_path, combined_files_vars):
                        continue

        # for graphid, graphname in zip(config_task[_CONST_CONFIG_GRAPHIDS], config_task[_CONST_CONFIG_GRAPHNAMES]):
    # ==================================================================================================================
    # ==================================================================================================================
    # End of the work cycle
    # ==================================================================================================================
    # __________________________________________________________________________
    return True


# ======================================================================================================================
# Functions
# ======================================================================================================================
def fs_check_access_dir(mode, *args):
    """
    Directory permission check.
    """
    return_value = True
    modes = {'ro': os.R_OK, 'rx': os.X_OK, 'rw': os.W_OK}
    for x in args:
        if not os.path.exists(x):
            print("[EE] Directory does not exist: {}".format(x), flush=True)
            return_value = False
        if not os.path.isdir(x):
            print("[EE] Is not directory: {}".format(x), flush=True)
            return_value = False
        if not os.access(x, modes[mode]):
            print("[EE] Directory access denied: {} ({})".format(x, mode), flush=True)
            return_value = False
    # __________________________________________________________________________
    return return_value


def timestamp_millis_64(my_datetime):
    return int(my_datetime.timestamp() * 1000)


def get_last_url_elem(url_string):
    if "/" not in url_string:
        return url_string
    found_values = url_string.split('/')
    return found_values[int(len(found_values) - 1)]


def get_proper_path(path_value):
    return re.sub('-|\\+|\\s|\\$|%|\\v|:|;|\\"|/|\\[|\\]|#|\\^|\\*|\\?|<|>', '', path_value)


def get_vars_from_files(var_files_array):
    combinations_array = []
    structured_vars_array = []
    # vars_pack = []

    if not var_files_array:
        return combinations_array

    # rows_sum = 1
    files_number = 0
    for var_file in var_files_array:
        with open(var_file, newline='') as csv_file:
            csv_reader = csv.reader(csv_file, delimiter=' ')
            if not csv_reader:
                continue
            # number of all rows in all files (count combinations)
            # rows_sum *= len(list(csv_reader)) - 1
            # number of all param files
            files_number += 1
            # array to collect all file rows
            one_file_vars_array = []
            # get first values from every file for final list
            # vars_pack.append(save_row_values(headers_array, csv_reader[1]))
            line_number = 0
            for row in csv_reader:
                if line_number == 0:
                    # first line - always vars names (headers), must be separated from the others
                    headers_array = row
                else:
                    # save current row values
                    one_file_vars_array.append(save_row_values(headers_array, row))
                line_number += 1

            # save all found values from current file into main array
            structured_vars_array.append(one_file_vars_array)

    if files_number == 0 or not structured_vars_array:
        return combinations_array

    # print("[..] parsing parameters.// counting var files: {}".format(files_number), flush=True)

    # forming array with lists of values combinations from all vars (parameter) values
    combined_values_array = list(itertools.product(*structured_vars_array))

    i = 0
    while i < len(combined_values_array):
        one_line_values = []
        all_files_combination = list(combined_values_array[i])
        for one_file_row in all_files_combination:
            for one_value in one_file_row:
                one_line_values.append(one_value)
        combinations_array.append(one_line_values)
        i += 1

    return combinations_array


def save_row_values(headers_array, row):
    # array to collect data from one row from files
    one_row_array = []
    # cycle per values in one row
    for header, value in zip(headers_array, row):
        # save every value from current row - count may vary
        one_row_array.append({_CONST_VAR_NAME: header, _CONST_VAR_VALUE: value})
    return one_row_array


def get_structured_panels_array(panels_array, target_panel_id_array):
    row_panel_id = -1
    structured_panels_array = []
    panels_under_row = []

    if not panels_array:
        return structured_panels_array

    i = 0
    while i < len(panels_array):
        panel_info = panels_array[i]
        # print("found graph id: {} name: {}".format(panel_id, panel_info['title']))

        # if found row - should be saved, if it's the new one - then previous row should be saved with all panels
        if 'row' == panel_info['type']:
            if panels_under_row:
                # print("saving panels. Row: {} // Panels: {}".format(row_panel_id, panels_under_row))
                structured_panels_array.append({_CONST_ROW_ID: row_panel_id,
                                                _CONST_PANELS_UNDER_ROW: list(panels_under_row).copy()})
                panels_under_row.clear()
            row_panel_id = panel_info['id']
            i += 1
            continue

        panel_id = panel_info['id']

        # check if this panel exists in target list (config params)
        if target_panel_id_array and (not check_panel_id(target_panel_id_array, panel_id)):
            i += 1
            continue

        # add found panel into list
        panels_under_row.append(panel_id)
        print("[..] found graph id: {} | name: {}".format(panel_id, panel_info['title']))

        # check if it's the last value and not row - then all pack of panels should be saved
        if i == len(panels_array)-1:
            if panels_under_row:
                structured_panels_array.append({_CONST_ROW_ID: row_panel_id,
                                                _CONST_PANELS_UNDER_ROW: list(panels_under_row).copy()})
                panels_under_row.clear()
                break

        i += 1
    # saving last panels on dashboard
    if panels_under_row:
        structured_panels_array.append({_CONST_ROW_ID: row_panel_id,
                                        _CONST_PANELS_UNDER_ROW: list(panels_under_row).copy()})
    print("[..] structured panels array:\n {}".format(structured_panels_array), flush=True)
    return structured_panels_array


def get_panel_name_from_json(current_panel_id, panels_array):
    panel_name = ''
    for panel_info in panels_array:
        if current_panel_id == panel_info['id']:
            panel_name = panel_info['title']
    return panel_name


def create_directories(* dir_name):
    for directory in dir_name:
        if not os.path.exists(directory):
            os.makedirs(directory)


def check_panel_id(target_array, check_id):
    if target_array:
        for target in target_array:
            # print("check ID: {} // target ID: {}".format(check_id, target))
            if int(check_id) == int(target):
                return True
    return False


def get_and_save_panel_png(config_task, grafana_dash_name, current_panel_name,
                   current_panel_id, time_from, time_till, current_path, variables):
    graphid = current_panel_id
    graphname = current_panel_name
    img_file_name_piece = ''
    # __________________________________________________________________
    cj = http.cookiejar.CookieJar()
    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))
    url = config_task[_CONST_CONFIG_GRAFANA_URL].rstrip('/')
    url += "/render/d-solo/{}".format(config_task[_CONST_CONFIG_DASH_ID])
    url += "/{}".format(grafana_dash_name)
    url += "?orgId={}".format(config_task[_CONST_CONFIG_ORG_ID])
    url += "&from={}&to={}".format(timestamp_millis_64(time_from), timestamp_millis_64(time_till))
    url += "&panelId={0}".format(graphid)
    url += "&width={}&height={}".format(config_task[_CONST_CONFIG_IMG_WIDTH],
                                        config_task[_CONST_CONFIG_ING_HEIGHT])
    # url += "&tz=Europe%2FMoscow"

    # add all variables into URL path and file name
    if variables:
        for one_var in variables:
            url += "&{}={}".format(one_var[_CONST_VAR_NAME], one_var[_CONST_VAR_VALUE])
            img_file_name_piece += "_{}".format(str(one_var[_CONST_VAR_VALUE]))
    # __________________________________________________________________
    img_file_name = "{}_{}.png".format(str(img_file_name_piece), str(graphname))
    unappropriated_path = os.path.join(current_path, img_file_name)
    img_file_path = get_proper_path(unappropriated_path)
    # __________________________________________________________________
    # Test mode
    # print("[..] {}".format(url), flush=True)
    # print("[..] -> {}".format(img_file_path), flush=True)
    # __________________________________________________________________
    opener.addheaders = [
        ('Authorization', str('Bearer ' + config_task[_CONST_CONFIG_BEARER_KEY])),
        ('Content-type', 'application/json'),
    ]
    # do response, catch http error
    try:
        response = opener.open(url, timeout=1000)
    except urllib.request.HTTPError:
        print("[EE] Download failed. Going to the next. Error with file: {}".format(img_file_path))
        return False
    content = response.read()
    # check response for another errors
    if response.code != 200 \
            or 'content-type' not in response.headers \
            or response.headers['content-type'] != 'image/png' \
            or content[0] != 137:
        print("[EE] Download failed:\nHTTP status code: {}\n{}".format(
            response.code,
            '\n'.join(["{}: {}".format(x, response.headers[x]) for x in response.headers]), flush=True))
        return False
    # __________________________________________________________________
    # Save
    with open(img_file_path, 'wb') as f:
        f.write(content)
        print("[OK] Graph id: {} saved: '{}'".format(graphid, img_file_path), flush=True)
    return True


# %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
if __name__ == '__main__':
    rc = main()
    # __________________________________________________________________________
    if os.name == 'nt':
        # noinspection PyUnresolvedReferences
        import msvcrt

        print("[..] Press any key to exit", flush=True)
        msvcrt.getch()
    # __________________________________________________________________________
    sys.exit(not rc)  # Compatible return code
