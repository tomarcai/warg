from django.utils.html import conditional_escape
from django.utils.safestring import mark_safe
from django import template

register = template.Library()

@register.filter
def xmlattr(value, autoescape=None):
    if autoescape:
        esc = conditional_escape
    else:
        esc = lambda x: x
    result = "&#10;".join(esc(value).splitlines())
    return mark_safe(result)

xmlattr.needs_autoescape = True

