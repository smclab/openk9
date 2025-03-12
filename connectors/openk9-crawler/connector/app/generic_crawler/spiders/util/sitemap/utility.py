import re


def regex(x):
    if isinstance(x, str):
        return re.compile(x)
    return x


def iterloc(it, alt=False):
    for d in it:
        yield d['loc']

        # Also consider alternate URLs (xhtml:link rel="alternate")
        if alt and 'alternate' in d:
            yield from d['alternate']


def iterlastmod(it, alt=False):
    for d in it:
        yield d['lastmod']

        # Also consider alternate URLs (xhtml:link rel="alternate")
        if alt and 'alternate' in d:
            yield from d['alternate']