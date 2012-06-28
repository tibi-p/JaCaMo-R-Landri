from zipfile import ZipFile
from zipfile import BadZipfile


class Validator(object):
    
    ORG_PREFIX = 'org/aria/'
    PROJECT_PREFIX = 'rlandri/'
    
    
    @staticmethod
    def validateAgentMapping(agentList):
        names = []
        for ag in agentList:
            if ag['agentId'] in names:
                return False
            else:
                names.append(ag['agentId'])
        return True
    
    @staticmethod
    def validateSolution(jarFile,userID):
        try:
            with ZipFile(jarFile, 'r') as jar:
                path = Validator.ORG_PREFIX+Validator.PROJECT_PREFIX+'user'+str(userID) + "/"
                for name in jar.namelist():
                    if 'META-INF/MANIFEST.MF' != name:
                        if not name.startswith(path):
                            return False
        except BadZipfile:
            return False
        return True
