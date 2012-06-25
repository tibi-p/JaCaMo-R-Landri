from django import forms

def make_base_custom_formset(queryset):
    class BaseCustomFormSet(forms.models.BaseModelFormSet):
        def __init__(self, *args, **kwargs):
            if queryset is not None and not 'queryset' in kwargs:
                kwargs['queryset'] = queryset
            super(BaseCustomFormSet, self).__init__(*args, **kwargs)

    return BaseCustomFormSet

def create_callback_post_save(field):
    original_field = 'original_%s' % (field,)

    def file_post_save(sender, instance, created, **kwargs):
        if not created:
            getattr(instance, original_field).delete(save=False)
        setattr(instance, original_field, getattr(instance, field))

    return file_post_save

def create_callback_post_delete(field):
    def file_post_delete(sender, instance, **kwargs):
        print 'file_post_delete'
        print 'fpd', getattr(instance, field)
        getattr(instance, field).delete(save=False)

    return file_post_delete
