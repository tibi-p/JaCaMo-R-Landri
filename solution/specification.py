from xml.dom.minidom import Document
from xml.dom.minidom import parse

class SolutionSpecification(object):

    '''  
    asl_list = [{'agentID':'ag1','cardinality':1, 'file':'ag1.asl'},
                {'agentID':'ag2','cardinality':2, 'file':'ag2.asl'}]
    artifacts_jar = 'artifacts.jar'
    org_zip = 'orgs.zip'
    '''
    
    def __init__(self,asl_list,artifacts_jar,org_zip):
        self.asl_list = asl_list
        self.artifacts_jar=artifacts_jar
        self.org_zip = org_zip
        
    
    #Generate XML file from object
    def make_xml(self):
        doc = Document()
        root = doc.createElement('solution')
        doc.appendChild(root)
        
        node = doc.createElement('asl-list')
        
        for dic in self.asl_list:
            agentNode = doc.createElement('asl')
            for attr in dic.keys():
                agentNode.setAttribute(attr,unicode(dic[attr]))
            node.appendChild(agentNode)
        root.appendChild(node)
        
        node = doc.createElement('artifacts')
        node.setAttribute('file',self.artifacts_jar)
        root.appendChild(node)
        
        node = doc.createElement('organizations')
        node.setAttribute('file',self.org_zip)
        root.appendChild(node)
        
        return doc
    
    
    
    def parse(self,xmlFile):  
      
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

