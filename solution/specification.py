from django.core.files.storage import default_storage
from xml.dom.minidom import Document, parse
from xml.etree.cElementTree import ElementTree, SubElement

class SolutionSpecification(object):

    @staticmethod
    def add_agents_to_xml(config_xml, asl_list):
        tree = ElementTree()
        tree.parse(config_xml)

        root = tree.getroot()
        nodes = root.findall('asl-list')
        for node in nodes:
            root.remove(node)
        node = SubElement(root, 'asl-list')
        for entry in asl_list:
            agentNode = SubElement(node, 'asl')
            for key, value in entry.iteritems():
                agentNode.attrib[key] = value

        return tree

    #Generate XML file from object
    @staticmethod
    def make_xml(solution):
        doc = Document()
        root = doc.createElement('solution')
        doc.appendChild(root)

        artifacts = solution.artifacts
        if artifacts:
            node = doc.createElement('artifacts')
            node.setAttribute('file', artifacts.name)
            root.appendChild(node)

        organizations = solution.organizations
        if organizations:
            node = doc.createElement('organizations')
            node.setAttribute('file', organizations.name)
            root.appendChild(node)

        return doc

    @staticmethod
    def parseAgentMapping(xmlFile):
        tree = ElementTree()
        tree.parse(xmlFile)
        nodes = tree.findall('asl-list/asl')
        return [ node_to_datatables(node) for node in nodes ]

    @staticmethod
    def parse(xmlFile):
        dom = parse(xmlFile)
        nodes = dom.getElementsByTagName('asl')
        agents = [ node_to_config(node) for node in nodes ]

        artifacts = None
        nodes = dom.getElementsByTagName('artifacts')
        if nodes:
            artifacts = node_attr_filepath(nodes[0], 'file')

        organizations = None
        nodes = dom.getElementsByTagName('organizations')
        if nodes:
            organizations = node_attr_filepath(nodes[0], 'file')

        return agents, artifacts, organizations

    @staticmethod
    def parse_repair_xml(solution):
        config_xml = solution.get_config_filepath()
        try:
            return SolutionSpecification.parse(config_xml)
        except IOError, e:
            # TODO log me
            print e
            dom = SolutionSpecification.make_xml(solution)
            with open(config_xml, 'w') as f:
                f.write(dom.toprettyxml())
            return SolutionSpecification.parse(config_xml)

def node_attr_filepath(node, attr):
    filename = node.getAttribute(attr)
    return default_storage.path(filename)

def node_to_datatables(node):
    attrib = node.attrib
    agent_name = attrib.get('agentId')
    filename = attrib.get('file')
    agent_class = attrib.get('agentClass', '')
    cardinality = attrib.get('cardinality')
    return [ [agent_name, ''], [filename, ''], [agent_class, ''],
        [cardinality, ''] ]

def node_to_config(node):
    agent_id = node.getAttribute('agentId')
    filename = node.getAttribute('file')
    agent_class = node.getAttribute('agentClass')
    cardinality = node.getAttribute('cardinality')
    return {
        'arch': 'c4jason.CAgentArch',
        'name': 'agent_%s_' % (agent_id,),
        'code': filename,
        'class': agent_class,
        'no': cardinality,
    }
