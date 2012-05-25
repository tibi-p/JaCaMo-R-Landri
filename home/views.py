from django.core.urlresolvers import reverse
from django.shortcuts import render_to_response
from django.template import RequestContext

class SiteSection(object):
    def __init__(self, name, view, bgcolor):
        self.name = name
        self.view = view
        self.bgcolor = bgcolor
        self.url = reverse(view)
        self.isCurrent = False

    def __str__(self):
        return '%s (%s, %s, %s)' % (self.name, self.view, self.url, self.isCurrent)

def home_context_processor(request):
    currentUrl = request.get_full_path()
    is_superuser = request.user.is_superuser
    sections = [ ]
    sections.append(SiteSection('Profile', 'envuser.views.profile', '#53b388'))
    sections.append(SiteSection('City', 'city.views.index', '#5a69a9'))
    if not is_superuser:
        sections.append(SiteSection('Solutions', 'solution.views.index', '#5a69a9'))
    sections.append(SiteSection('Ranking', 'envuser.views.index', '#c26468'))
    sections.append(SiteSection('Simulator', 'simulator.views.simulate', '#bf7cc7'))
    sections.append(SiteSection('SubEnvironment', 'subenvironment.views.index', '#bf7cc7'))
    if is_superuser:
        sections.append(SiteSection('Admin', 'admin:index', '#111111'))

    sortedSections = sorted(sections, key=lambda x: len(x.url), reverse=True)
    currentSection = None
    for section in sortedSections:
        if currentUrl.startswith(section.url):
            section.isCurrent = True
            currentSection = section
            break

    return {
        'currentUrl': currentUrl,
        'sections': sections,
        'currentSection': currentSection,
    }

def index(request):
    return render_to_response('home/index.html',
        { },
        context_instance = RequestContext(request))
