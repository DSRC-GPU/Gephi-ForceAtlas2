#!/usr/bin/env python2.7

class Fields:
    ID                = "ID"
    GROUP             = "GROUP"
    CC                = "CC"
    FINE_PCA          = "FINE_PCA"
    COARSE_PCA        = "COARSE_PCA"
    EMBEDDING_POS     = "EMBEDDING_POS"
    VELOCITY_VECTOR   = "VELOCITY_VECTOR"
    FINE_SMOOTH_VEL   = "FINE_SMOOTH_VEL"
    COARSE_SMOOTH_VEL = "COARSE_SMOOTH_VEL"

    SCALAR = "SCALAR"
    VECTOR = "VECTOR"

    def __init__(self, lines):
        self.types = {}
        self.parse_types(lines)

    def convert(self, value, type_):
        import importlib
        try:
            # Check if it's a builtin type
            module = importlib.import_module('__builtin__')
            cls = getattr(module, type_)
        except AttributeError:
            # if not, separate module and class
            module, type_ = type_.rsplit(".", 1)
            module = importlib.import_module(module)
            cls = getattr(module, type_)
        return cls(value)

    def parse_types(self, lines):
        for line in lines:
            parts = line.split()
            assert len(parts) == 3
            field_name = parts[0]
            field_type = parts[1]
            field_cast = parts[2]
            self.types[field_name] = field_type, field_cast

    def parse_line(self, name, val):
        field_type, field_cast = self.types[name]
        if field_type == self.SCALAR:
            return self.convert(val, field_cast)
        elif field_type == self.VECTOR:
            parts = val.split()
            return [self.convert(p, field_cast) for p in parts]
        else:
            raise Exception("Unknown field type: " + field_type)

    @staticmethod
    def get_vectors():
        return [Fields.EMBEDDING_POS, Fields.VELOCITY_VECTOR, Fields.FINE_SMOOTH_VEL, Fields.COARSE_SMOOTH_VEL]

    @staticmethod
    def get_attrs():
        return [Fields.GROUP, Fields.CC]
