from models import get_config_filepath
from xml.dom.minidom import Document, parse

class SolutionSpecification(object):

    @staticmethod
    def add_agents_to_xml(xmlFile, asl_list):
        doc = parse(xmlFile)
        
        root = doc.getElementsByTagName("solution")[0]
        old = root.getElementsByTagName("asl-list")
        if old:
            doc.documentElement.removeChild(old[0])
        node = doc.createElement('asl-list')
        
        for dic in asl_list:
            agentNode = doc.createElement('asl')
            for key, value in dic.iteritems():
                agentNode.setAttribute(key, value)
            node.appendChild(agentNode)
        root.appendChild(node)
        
        return doc
    
    #Generate XML file from object
    @staticmethod
    def make_xml(artifacts_jar, org_zip):
        doc = Document()
        root = doc.createElement('solution')
        doc.appendChild(root)
        
        node = doc.createElement('artifacts')
        node.setAttribute('file', artifacts_jar)
        root.appendChild(node)
        
        node = doc.createElement('organizations')
        node.setAttribute('file', org_zip)
        root.appendChild(node)
        
        return doc
    
    
    @staticmethod
    def parseAgentMapping(xmlFile):
        lst = []
        dom = parse(xmlFile)
        nodes = dom.getElementsByTagName('asl')
        for node in nodes:
            agentName = node.getAttribute('agentId')
            file = node.getAttribute('file')
            cardinality = node.getAttribute('cardinality')
            lst.append([[agentName, ''], [file, ''], [cardinality, '']])
        return lst

    @staticmethod
    def parse(xmlFile):
      
        agents = []
        agentFileNames = []
      
        dom = parse(xmlFile)
       
        
        nodes = dom.getElementsByTagName('asl')
        for node in nodes:
            agentId = node.getAttribute('agentID')
            agentName = "agent_" + str(agentId)
            
            cardinality = node.getAttribute('cardinality')
            
            agentFileNames.append(node.getAttribute('file'))
            agents.append({
                'arch' : 'c4jason.CAgentArch',
                'name' : agentName,
                'no' : cardinality
                           })
        
        node = dom.getElementsByTagName('artifacts')[0]
        artifactsJar = node.getAttribute('file')
        
        node = dom.getElementsByTagName('organizations')[0]
        orgsZip = node.getAttribute('file')
            
        return agents, agentFileNames, artifactsJar, orgsZip

    @staticmethod
    def parse_repair_xml(solution):
        config_xml = get_config_filepath(solution)
        try:
            return SolutionSpecification.parse(config_xml)
        except IOError, e:
            # TODO log me
            print e
            dom = SolutionSpecification.make_xml(solution.artifacts.path, solution.organizations.path)
            with open(config_xml, 'w') as f:
                f.write(dom.toprettyxml())
            return SolutionSpecification.parse(config_xml)
