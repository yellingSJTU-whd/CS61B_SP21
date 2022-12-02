import requests
import json
import os
from log import print_log, WARNING, ERROR


HEADERS = {'content-type': 'application/json', 'user-agent': 'op-ops-tools'}

GET_ROLE_ALL = False
MSB_INFO = {}
IS_TOB_SCENE = False


def get_role_target_nodes(input_roles, input_ips):
    '''
    cloud_nodes is dict, key is defalut_ip, value is dict
    {ip1: {roles: [master], all_ip: [ip1, ip2]}},
    target_nodes is dict, elem is ip:role_list
    '''

    cloud_nodes = get_env_node_info(GET_ROLE_ALL)
    if not cloud_nodes:
        if not input_ips:
            return {}

        if not input_roles:
            return {ip: [] for ip in input_ips}
        return {ip: input_roles for ip in input_ips}

    if input_ips:
        target_nodes = filter_node_by_ip(cloud_nodes, input_ips)
    else:
        target_nodes = {key: value.get('roles') for key, value in
                        cloud_nodes.items()}

    if input_roles:
        target_nodes = filter_node_by_roles(
            target_nodes, input_ips, input_roles)

    return target_nodes


def get_env_node_info(need_read_file=False):
    nodes = get_env_node_info_by_api()
    if nodes:
        return nodes

    print_log("[WARNING] Cannot get node's role info.", WARNING)

    if need_read_file:
        nodes = get_env_node_info_by_file()
    return nodes


def get_env_node_info_by_api():
    node_info = {}
    msb_ip = MSB_INFO.get('ip')
    msb_port = MSB_INFO.get('port')

    if not msb_ip or not msb_port:
        return node_info

    if IS_TOB_SCENE:
        return get_node_info_from_toposerver(msb_ip, msb_port)
    else:
        return get_node_info_from_network(msb_ip, msb_port)


def get_node_info_from_toposerver(msb_ip, msb_port):
    node_info = {}
    try:
        url = "http://%s:%s/toposerver/v1/tenants/%s/resrelation" % (
            msb_ip, msb_port, 'admin')
        params = {"type": ["node"]}
        rsp = requests.post(url, headers=HEADERS,
                            data=json.dumps(params), timeout=10)

        if not rsp or rsp.status_code != 200:
            print_log("get node's role from toposerver error. status_code:%s"
                      % (rsp.status_code), WARNING, False)
            return node_info

        text = rsp.json()
        if text.get('result') != 'ok':
            print_log("get node's role from toposerver error. result: %s"
                      % (text.get('result')), WARNING, False)
            return node_info

        for elem in text.get('data', []):
            if elem.get('ip'):
                node_info[elem.get('ip')] = {'roles': elem.get('roles', []),
                                             'all_ip': [elem.get('ip')]}
        return node_info
    except Exception as e:
        print_log("[FAILED] Get environment node infomation code error. "
                  "case: %s " % e, ERROR)
        return {}


def get_node_info_from_network(msb_ip, msb_port):
    node_info = {}
    try:
        url = "http://%s:%s/nodeworker/v1/tenants/admin/nodes" % (
            msb_ip, msb_port)
        nodes_info = client_get(url)

        if not isinstance(nodes_info, dict):
            return {}

        nodes = nodes_info.get('nodes')
        if not isinstance(nodes, list):
            return {}

        for node in nodes:
            defaule_ip, all_ip = get_node_ip(node)
            if defaule_ip is None:
                continue
            roles = node.get('roles', [])
            node_info[defaule_ip] = {'roles': roles, 'all_ip': all_ip}
        return node_info
    except Exception as e:
        print_log("[FAILED] Get environment node infomation code error. "
                  "case: %s " % e, ERROR)
        return {}


def get_node_ip(node):
    default_ip, ip_v4, ip_v6 = None, None, None

    try:
        netinfo = node.get('netinfo', {})
        try:
            default_ip = netinfo.get('net_api').get('ip')
            ip_v4 = netinfo.get('net_api_v4', {}).get('ip')
            ip_v6 = netinfo.get('net_api_v6', {}).get('ip')
        except Exception:
            default_ip = None

        if default_ip:
            return default_ip, [default_ip, ip_v4, ip_v6]

        try:
            default_ip = netinfo.get('net_admin').get('ip')
        except Exception:
            default_ip = None
        return default_ip, [default_ip]

    except Exception:
        return default_ip, [default_ip]


def get_env_node_info_by_file():
    nodes = {}
    hosts_file = '/etc/pdm/hosts'

    if not os.path.exists(hosts_file):
        print_log("[WARNING] Cannot get controller node because"
                  " file /etc/pdm/hosts not exists.", WARNING)
        return nodes

    try:
        with open(hosts_file, 'r') as f:
            lines = f.readlines()
            for line in lines[1:]:
                elems = line.split(' ')
                if len(elems) > 1:
                    nodes[elems[0]] = {'roles': ['paas_controller'],
                                       'all_ip': [elems[0]]}
    except Exception as e:
        print_log("[FAILED] Read /etc/pdm/hosts code error. case: %s " % e,
                  ERROR)
    return nodes


def client_get(url):
    try:
        rsp = requests.get(url=url, headers=HEADERS, timeout=5)
        if rsp and rsp.status_code == 200:
            return json.loads(rsp.text)
        print_log("request url:%s status_code is:%s" % (url, rsp.status_code),
                  ERROR, False)
        return None
    except Exception as e:
        print_log("request url:%s error. cause:%s" % (url, e), ERROR, False)
        return None


def filter_node_by_ip(cloud_nodes, input_ips):
    target_nodes = {}
    for ip in input_ips:
        flag = False
        for key, value in cloud_nodes.items():
            if ip in value.get('all_ip', []):
                target_nodes[ip] = value.get('roles', [])
                flag = True
                break
        if not flag:
            print_log("[WARNING] User input ip %s incorrect, so collection "
                      "on this node will not continue." % (ip), WARNING)
    return target_nodes


def filter_node_by_roles(cloud_nodes, input_ips, input_roles):
    target_nodes = {}
    matched_roles = []

    for ip, roles in cloud_nodes.items():
        if roles:
            node_input_roles = list(set(roles) & set(input_roles))
        else:
            # ?????????????????????????
            # ??????????????????
            node_input_roles = input_roles

        if node_input_roles:
            target_nodes[ip] = node_input_roles
            matched_roles.extend(node_input_roles)
            continue
        if input_ips and ip in input_ips:
            print_log("[WARNING] node %s have no role %s, so collection "
                      "on this node will not continue." % (ip, input_roles),
                      WARNING)

    print_fileterd_roles(input_roles, matched_roles)
    return target_nodes


def print_fileterd_roles(input_roles, matched_roles):
    filtered_roles = [role for role in input_roles
                      if role not in set(matched_roles)]
    for role in filtered_roles:
        print_log("[WARNING] Can not get the node of the role:%s" % (role),
                  WARNING)