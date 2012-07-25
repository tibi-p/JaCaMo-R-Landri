from zipfile import BadZipfile, ZipFile

class Validator(object):

    ORG_PREFIX = 'org/aria/'
    PROJECT_PREFIX = 'rlandri/'

    @staticmethod
    def validateAgentMapping(agents):
        names = set()
        for ag in agents:
            if ag['agentId'] in names:
                return False
            else:
                names.add(ag['agentId'])
        return True

    @staticmethod
    def validateSolution(jarFile, userID):
        try:
            with ZipFile(jarFile, 'r') as jar:
                path = Validator.ORG_PREFIX + Validator.PROJECT_PREFIX + 'user' + str(userID) + "/"
                for name in jar.namelist():
                    if 'META-INF/MANIFEST.MF' != name:
                        if not name.startswith(path):
                            return False
        except BadZipfile:
            return False
        return True

    @staticmethod
    def validateAgentZip(agentZip, userID):
        try:
            with ZipFile(agentZip, 'r') as zipFile:
                for name in zipFile.namelist():
                    if not name.endswith('_u' + str(userID) + '.asl'):
                        return False
        except BadZipfile:
            return False
        return True
