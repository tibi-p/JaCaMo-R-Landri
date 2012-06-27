from zipfile import ZipFile
from zipfile import BadZipfile


class Validator(object):
    
    ORG_PREFIX = 'org/aria/'
    PROJECT_PREFIX = 'rlandri/'
    
    @staticmethod
    def validateSolution(jarFile,userID):
        try:
          with ZipFile(jarFile, 'r') as jar:
            path = Validator.ORG_PREFIX+Validator.PROJECT_PREFIX+'user'+str(userID)
            for name in jar.namelist():
                if 'META-INF/MANIFEST.MF' != name:
                    if not name.startswith(path):
                        return False
        except BadZipfile:
            return False
        return True
