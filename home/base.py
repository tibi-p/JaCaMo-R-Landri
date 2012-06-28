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
        old_item = getattr(instance, original_field)
        new_item = getattr(instance, field)
        if old_item != new_item:
            if not created:
                safe_delete(old_item)
            setattr(instance, original_field, new_item)

    return file_post_save

def create_callback_post_delete(field):
    def file_post_delete(sender, instance, **kwargs):
        safe_delete(getattr(instance, field))

    return file_post_delete

def safe_delete(item):
    try:
        item.delete(save=False)
    except OSError, e:
        # TODO log me
        print e

def fill_object(row, attributes):
    for key, value in attributes.iteritems():
        setattr(row, key, value)
    row.save()
