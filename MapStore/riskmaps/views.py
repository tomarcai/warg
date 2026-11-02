from django.shortcuts import render_to_response, redirect, HttpResponse
from riskmaps.forms import GameMapForm, GameMapZipForm, AuthorForm, UnauthorisedAuthorForm
from riskmaps.models import GameMap, GameCategory, DailyStats
from riskmaps.util import MapDirHelper, update_map_model_from_zip
from django.views.decorators.csrf import csrf_exempt
from django.contrib.auth.decorators import login_required
from django.contrib.auth.models import User
from django.views.decorators.csrf import csrf_exempt
from django.db.models import Q
from settings import MEDIA_URL
import re

def map_count(request):
    map_url = request.REQUEST['url']
    
    results = GameMap.objects.filter(mapFile=map_url)
    
    if len(results) > 0:
        map_model = results[0]
        map_model.numberOfDownloads += 1
        map_model.save()

        daily_stat = DailyStats.stat_for_today()
        daily_stat.numberOfDownloads += 1
        daily_stat.save()
 
        return HttpResponse(map_model.mapFile.read())
    else:
        raise Exception(map_url)


@csrf_exempt
def list_all_maps(request):

    query = GameMap.objects
    category = None
    search = None
    author = None

    # Sort Order
    try:
        order_by = {
            "TOP_NEW":"-dateAdded",
            "TOP_RATINGS":"-rating_score",
            "TOP_DOWNLOADS":"-numberOfDownloads"
        }[request.REQUEST['sort']]

        query = query.order_by(order_by)

    except KeyError:
        pass

    # Filter Category
    try:
        category = GameCategory.objects.get( id=request.REQUEST['category'] )
        query = query.filter(categories=category)

    except KeyError:
        pass


    # Filter Author
    try:
        author = User.objects.get( id=request.REQUEST['author'] )
        query = query.filter(author=author)

    except KeyError:
        pass



    # Specific Map Search
    if request.method == 'GET':
        map_files = request.GET.getlist('mapfile')
    else:
        map_files = request.POST.getlist('mapfile')

    if len(map_files) > 0:
        # use a single regex rather than OR-ing a LIKE per file, as clients can send
        # over 1000 files, and that exceeds the database expression depth/variable limits
        pattern = '/(' + '|'.join([re.escape(map_file) for map_file in map_files]) + ')'
        query = query.filter(mapFile__regex = pattern)


    # Filter Published State
    query = query.filter(visible=True)


    # Determine Format
    format = "html"

    try:
        format = request.REQUEST['format']
    except KeyError:
        pass

    # Run Query
    try:
        search = request.REQUEST['search']
        query = query.filter(Q(name__icontains=search) | Q(description__icontains=search) | Q(author__username__icontains=search))

    except KeyError:
        pass
    
    template_name = {
        "xml"  : "map_list.xml",
        "html" : "list.html",
        "shtml": "getmaps.shtml",
        }[format]

    mime_type = {
        "xml"  : "application/xml",
        "html" : "text/html",
        "shtml": "text/html",
        }[format]

    return render_to_response(
        template_name,
        {
            # select_related fetches each map's author in the same query, instead of one query per map
            'map_list': query.select_related('author'),
            'search_category': category,
            'search_author' : author,
            'search_text' : search
        },
        mimetype=mime_type )


def list_all_categories(request):

    categories = GameCategory.objects.all();

    category_tuples = []

    for category in categories:
        try:
            map = GameMap.objects.filter(categories=category).order_by("rating_score").all()[:1][0]
        except:
            map = None
            
        category_tuples.append((category, map))



    format = "html"

    try:
        format = request.REQUEST['format']
    except KeyError:
        pass

    template_name = {
        "xml"  : "categories.xml",
        "html" : "categories.html",
        }[format]

    mime_type = {
        "xml"  : "application/xml",
        "html" : "text/html",
        }[format]

    return render_to_response(template_name, {'category_tuples':category_tuples}, mimetype=mime_type)


def view_map(request, map_id):
    return render_to_response("view_map.html", {'map':GameMap.objects.get(id=map_id)})

@login_required
@csrf_exempt
def upload_map(request):
    if request.method == 'POST':
        mapForm = GameMapForm(request.POST, request.FILES)
        zipForm = GameMapZipForm(request.POST, request.FILES)
        authorForm = AuthorForm(request.POST,instance=request.user)

        if mapForm.is_valid():
            newMap = mapForm.save(commit=False)
            #FIXME: Assign User
            #newMap.author = author

            newMap.author = request.user

            if authorForm.is_valid():
                authorForm.save()
            
            if zipForm.is_valid():
                zipData = zipForm.cleaned_data['mapZipFile'];
                if zipData:
                    update_map_model_from_zip(newMap, zipData)

            newMap.save()
            mapForm.save_m2m()

            return redirect('/')

    else:
        mapForm = GameMapForm()
        zipForm = GameMapZipForm()
        authorForm = AuthorForm(instance=request.user)

    return render_to_response("upload.html", {
        'mapForm': mapForm,
        'zipForm': zipForm,
        'authorForm': authorForm,

    })


@csrf_exempt
def upload_unauthorised_map(request):

    if request.method == 'POST':
        mapForm = GameMapForm(request.POST, request.FILES)
        zipForm = GameMapZipForm(request.POST, request.FILES)
        authorForm = UnauthorisedAuthorForm(request.POST)

        if mapForm.is_valid():
            newMap = mapForm.save(commit=False)

            if authorForm.is_valid() and len(authorForm.cleaned_data['email']) > 0:
                author = authorForm.save(commit=False)
            	existing_author = User.objects.filter(email=author.email)
                if existing_author:
                    author = existing_author[0]

                author.username = author.email
		author.save()
                newMap.author = author
            else:
                raise Exception(authorForm)

            if zipForm.is_valid():
                zipData = zipForm.cleaned_data['mapZipFile'];
                if zipData:
                    update_map_model_from_zip(newMap, zipData)
            else:
                raise Exception(zipForm)

            newMap.save()
            mapForm.save_m2m()

            import logging
            try:
                import mechanize
                br = mechanize.Browser()
                r = br.open('https://msg.yura.net/cgi-sys/FormMail.cgi', 'recipient=yura@yura.net&subject=NewMap&email=newmap@maps.yura.net&mapId=' + str(newMap.id) + '&mapName=' + newMap.name)
            except:
                logging.exception("formmail error")
                pass # Failed email is not a priority

            return redirect('/')

        else:
	    raise Exception(mapForm)
    else:
        mapForm = GameMapForm()
        zipForm = GameMapZipForm()
        authorForm = UnauthorisedAuthorForm()

    return render_to_response("unauthorised_upload.html", {
        'mapForm': mapForm,
        'zipForm': zipForm,
        'authorForm': authorForm,

    })

