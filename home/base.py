from django import forms

def make_base_custom_formset(queryset):
    class BaseCustomFormSet(forms.models.BaseModelFormSet):
        def __init__(self, *args, **kwargs):
            if queryset is not None and not 'queryset' in kwargs:
                kwargs['queryset'] = queryset
            super(BaseCustomFormSet, self).__init__(*args, **kwargs)

    return BaseCustomFormSet
