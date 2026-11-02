import os
from os.path import basename
import re
from django.template.defaultfilters import slugify

class MapDirHelper:

    def __init__(self, dirPath):
        self.dirPath = dirPath
        self.outlinesPath = ""
        self.cardsPath = ""
        self.picturePath = ""
        self.mapPath = ""
	self.previewPath = ""
	self.version = ""

        self.attributes = {
            "outlinesPath": "^map (.*)",
            "cardsPath" : "^crd (.*)",
            "picturePath" : "^pic (.*)",
	        "previewPath" : "^prv (.*)",
	        "version" : "^ver ([0-9]*)",
        }

        self._update_paths()


    def _get_map_path(self):
        for fileName in os.listdir(self.dirPath):
            if fileName.endswith(".map"):
                return self.dirPath + "/" + fileName

    def _update_paths(self):
        self.mapPath = self._get_map_path()

        mapData = open(self.mapPath).read()

        for attribute in self.attributes.iteritems():
            key, pattern = attribute;
            result = re.search(pattern, mapData, flags=re.MULTILINE)

            try:
                foundValue = result.groups()[0].strip()

                if key == "previewPath":
                    setattr(self, key, self.dirPath + "/preview/" + foundValue)
                elif key == "version":
                    setattr(self, key, foundValue)
                else:
                    setattr(self, key, self.dirPath + "/" + foundValue)

            except:
                pass # Nothing found



def update_map_model_from_zip(mapModel, zipPath):
    """
    Takes a Map model and updates its files from a given zip file path
    - Assumes author field is valid
    """
    from zipfile import ZipFile
    from django.core.files import File
    import tempfile


    zipFile = ZipFile(zipPath)
    zipFile.testzip()

    tempDir = tempfile.mkdtemp()
    zipFile.extractall(path=tempDir)


    helper = MapDirHelper(tempDir)

    try:
        map = File(open(helper.mapPath))
        outlines = File(open(helper.outlinesPath))
        image = File(open(helper.picturePath))
    except IOError:
        print "Error: Cannot unpack map, skipping: " + helper.mapPath
        return

    mapModel.mapFile.save(basename(helper.mapPath), map)
    mapModel.outlinesFile.save(basename(helper.outlinesPath), outlines)
    
    try:
        cards = File(open(helper.cardsPath))
        mapModel.cardsFile.save(basename(helper.cardsPath), cards)
    except IOError:
        pass # Cards is optional

    try:
        prev = File(open(helper.previewPath))
        mapModel.prevFile.save(basename(helper.previewPath), prev)
    except IOError:
        pass # Also optional
 

    mapModel.imageFile.save(basename(helper.picturePath), image)

    try:
        mapModel.version = int(helper.version)
    except ValueError:
        pass

