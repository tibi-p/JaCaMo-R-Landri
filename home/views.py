from django.core.urlresolvers import reverse
from django.shortcuts import render_to_response
from django.template import RequestContext

class SiteSection(object):
    def __init__(self, name, view):
        self.name = name
        self.view = view
        self.url = reverse(view)
        self.isCurrent = False

    def __str__(self):
        return '%s (%s, %s, %s)' % (self.name, self.view, self.url, self.isCurrent)

def home_context_processor(request):
    currentUrl = request.get_full_path()
    sections = [ ]
    sections.append(SiteSection('City', 'city.views.index'))
    sections.append(SiteSection('SubEnvironment', 'subenvironment.views.index'))
    sections.append(SiteSection('User', 'envuser.views.index'))
    sections.append(SiteSection('Simulator', 'simulator.views.simulate'))
    if request.user.is_superuser:
        sections.append(SiteSection('Admin', 'admin:index'))

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
