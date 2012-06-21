from zipfile import ZipFile
class Validator(object):
    def validateSolution(self,jarFile,userID):
        jar = ZipFile(jarFile, 'r')
        path = 'org/aria/rlandri/user'+str(userID)
        for name in jar.namelist():
            if cmp(name,'META-INF/MANIFEST.MF') != 0:
                if not name.startswith(path):
                    return False
        return True
               # if(!name.startswith('org/aria/rlandri/user'))

val = Validator()
print val.validateSolution("dummy.jar",10)