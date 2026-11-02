from riskmaps.models import GameMap
from django import forms
from django.contrib.auth.models import User


class GameMapForm(forms.ModelForm):
    class Meta:
        model = GameMap
        #exclude = ('author', 'version', 'dateAdded', 'numberOfDownloads')
        fields = ('name', 'description', 'categories')
        #TODO: Support for separate files OR zip file


class GameMapZipForm(forms.Form):
    mapZipFile = forms.FileField(label="Map Zip File")


class AuthorForm(forms.ModelForm):
    class Meta:
        model = User
        fields = ('first_name', 'last_name')
        
class UnauthorisedAuthorForm(forms.ModelForm):
    class Meta:
        model = User
        fields = ('first_name', 'email')
        
