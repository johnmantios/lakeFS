# coding: utf-8

"""
    lakeFS API

    lakeFS HTTP API  # noqa: E501

    The version of the OpenAPI document: 0.1.0
    Contact: services@treeverse.io
    Generated by: https://openapi-generator.tech
"""

from datetime import date, datetime  # noqa: F401
import decimal  # noqa: F401
import functools  # noqa: F401
import io  # noqa: F401
import re  # noqa: F401
import typing  # noqa: F401
import typing_extensions  # noqa: F401
import uuid  # noqa: F401

import frozendict  # noqa: F401

from lakefs_client import schemas  # noqa: F401


class MergeResultSummary(
    schemas.DictSchema
):
    """NOTE: This class is auto generated by OpenAPI Generator.
    Ref: https://openapi-generator.tech

    Do not edit the class manually.
    """


    class MetaOapg:
        
        class properties:
            added = schemas.IntSchema
            removed = schemas.IntSchema
            changed = schemas.IntSchema
            conflict = schemas.IntSchema
            __annotations__ = {
                "added": added,
                "removed": removed,
                "changed": changed,
                "conflict": conflict,
            }
    
    @typing.overload
    def __getitem__(self, name: typing_extensions.Literal["added"]) -> MetaOapg.properties.added: ...
    
    @typing.overload
    def __getitem__(self, name: typing_extensions.Literal["removed"]) -> MetaOapg.properties.removed: ...
    
    @typing.overload
    def __getitem__(self, name: typing_extensions.Literal["changed"]) -> MetaOapg.properties.changed: ...
    
    @typing.overload
    def __getitem__(self, name: typing_extensions.Literal["conflict"]) -> MetaOapg.properties.conflict: ...
    
    @typing.overload
    def __getitem__(self, name: str) -> schemas.UnsetAnyTypeSchema: ...
    
    def __getitem__(self, name: typing.Union[typing_extensions.Literal["added", "removed", "changed", "conflict", ], str]):
        # dict_instance[name] accessor
        return super().__getitem__(name)
    
    
    @typing.overload
    def get_item_oapg(self, name: typing_extensions.Literal["added"]) -> typing.Union[MetaOapg.properties.added, schemas.Unset]: ...
    
    @typing.overload
    def get_item_oapg(self, name: typing_extensions.Literal["removed"]) -> typing.Union[MetaOapg.properties.removed, schemas.Unset]: ...
    
    @typing.overload
    def get_item_oapg(self, name: typing_extensions.Literal["changed"]) -> typing.Union[MetaOapg.properties.changed, schemas.Unset]: ...
    
    @typing.overload
    def get_item_oapg(self, name: typing_extensions.Literal["conflict"]) -> typing.Union[MetaOapg.properties.conflict, schemas.Unset]: ...
    
    @typing.overload
    def get_item_oapg(self, name: str) -> typing.Union[schemas.UnsetAnyTypeSchema, schemas.Unset]: ...
    
    def get_item_oapg(self, name: typing.Union[typing_extensions.Literal["added", "removed", "changed", "conflict", ], str]):
        return super().get_item_oapg(name)
    

    def __new__(
        cls,
        *_args: typing.Union[dict, frozendict.frozendict, ],
        added: typing.Union[MetaOapg.properties.added, decimal.Decimal, int, schemas.Unset] = schemas.unset,
        removed: typing.Union[MetaOapg.properties.removed, decimal.Decimal, int, schemas.Unset] = schemas.unset,
        changed: typing.Union[MetaOapg.properties.changed, decimal.Decimal, int, schemas.Unset] = schemas.unset,
        conflict: typing.Union[MetaOapg.properties.conflict, decimal.Decimal, int, schemas.Unset] = schemas.unset,
        _configuration: typing.Optional[schemas.Configuration] = None,
        **kwargs: typing.Union[schemas.AnyTypeSchema, dict, frozendict.frozendict, str, date, datetime, uuid.UUID, int, float, decimal.Decimal, None, list, tuple, bytes],
    ) -> 'MergeResultSummary':
        return super().__new__(
            cls,
            *_args,
            added=added,
            removed=removed,
            changed=changed,
            conflict=conflict,
            _configuration=_configuration,
            **kwargs,
        )
