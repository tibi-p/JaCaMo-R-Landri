from xml.dom.minidom import Document
from xml.dom.minidom import parse

class SolutionSpecification(object):

    '''  
    asl_list = [{'agentID':'ag1','cardinality':1, 'file':'ag1.asl'},
                {'agentID':'ag2','cardinality':2, 'file':'ag2.asl'}]
    artifacts_jar = 'artifacts.jar'
    org_zip = 'orgs.zip'
    '''
    
    @staticmethod
    def add_agents_to_xml(xmlFile,asl_list):
        doc =  parse(xmlFile)
        
        root=doc.getElementsByTagName("solution")[0]
        old= root.getElementsByTagName("asl-list")[0]
        doc.documentElement.removeChild(old)
        node = doc.createElement('asl-list')
        
        for dic in asl_list:
            agentNode = doc.createElement('asl')
            for attr in dic.keys():
                agentNode.setAttribute(attr,unicode(dic[attr]))
            node.appendChild(agentNode)
        root.appendChild(node)
        
        return doc
    
    #Generate XML file from object
    @staticmethod
    def make_xml(artifacts_jar,org_zip):
        doc = Document()
        root = doc.createElement('solution')
        doc.appendChild(root)
        
        node = doc.createElement('artifacts')
        node.setAttribute('file',artifacts_jar)
        root.appendChild(node)
        
        node = doc.createElement('organizations')
        node.setAttribute('file',org_zip)
        root.appendChild(node)
        
        return doc
    
    
    @staticmethod
    def parse(xmlFile):  
      
        agents = []
        agentFileNames = []
      
        dom =  parse(xmlFile)
       
        
        nodes = dom.getElementsByTagName('asl')
        for node in nodes:
            agentId = node.getAttribute('agentID')
            agentName = "agent_"+str(agentId)
            
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
            
        return agents,agentFileNames,artifactsJar,orgsZip

