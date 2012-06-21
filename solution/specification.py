from xml.dom.minidom import Document

class SolutionSpecification(object):
    
    asl_list = [{'agentID':'ag1','cardinality':1, 'file':'ag1.asl'},
                {'agentID':'ag2','cardinality':2, 'file':'ag2.asl'}]
    artifacts_jar = 'artifacts.jar'
    org_zip = 'orgs.zip'
    
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
