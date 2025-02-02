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


class MetaRangeCreation(
    schemas.DictSchema
):
    """NOTE: This class is auto generated by OpenAPI Generator.
    Ref: https://openapi-generator.tech

    Do not edit the class manually.
    """


    class MetaOapg:
        required = {
            "ranges",
        }
        
        class properties:
            
            
            class ranges(
                schemas.ListSchema
            ):
            
            
                class MetaOapg:
                    min_items = 1
                    
                    @staticmethod
                    def items() -> typing.Type['RangeMetadata']:
                        return RangeMetadata
            
                def __new__(
                    cls,
                    _arg: typing.Union[typing.Tuple['RangeMetadata'], typing.List['RangeMetadata']],
                    _configuration: typing.Optional[schemas.Configuration] = None,
                ) -> 'ranges':
                    return super().__new__(
                        cls,
                        _arg,
                        _configuration=_configuration,
                    )
            
                def __getitem__(self, i: int) -> 'RangeMetadata':
                    return super().__getitem__(i)
            __annotations__ = {
                "ranges": ranges,
            }
    
    ranges: MetaOapg.properties.ranges
    
    @typing.overload
    def __getitem__(self, name: typing_extensions.Literal["ranges"]) -> MetaOapg.properties.ranges: ...
    
    @typing.overload
    def __getitem__(self, name: str) -> schemas.UnsetAnyTypeSchema: ...
    
    def __getitem__(self, name: typing.Union[typing_extensions.Literal["ranges", ], str]):
        # dict_instance[name] accessor
        return super().__getitem__(name)
    
    
    @typing.overload
    def get_item_oapg(self, name: typing_extensions.Literal["ranges"]) -> MetaOapg.properties.ranges: ...
    
    @typing.overload
    def get_item_oapg(self, name: str) -> typing.Union[schemas.UnsetAnyTypeSchema, schemas.Unset]: ...
    
    def get_item_oapg(self, name: typing.Union[typing_extensions.Literal["ranges", ], str]):
        return super().get_item_oapg(name)
    

    def __new__(
        cls,
        *_args: typing.Union[dict, frozendict.frozendict, ],
        ranges: typing.Union[MetaOapg.properties.ranges, list, tuple, ],
        _configuration: typing.Optional[schemas.Configuration] = None,
        **kwargs: typing.Union[schemas.AnyTypeSchema, dict, frozendict.frozendict, str, date, datetime, uuid.UUID, int, float, decimal.Decimal, None, list, tuple, bytes],
    ) -> 'MetaRangeCreation':
        return super().__new__(
            cls,
            *_args,
            ranges=ranges,
            _configuration=_configuration,
            **kwargs,
        )

from lakefs_client.model.range_metadata import RangeMetadata
