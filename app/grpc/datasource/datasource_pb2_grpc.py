# Generated by the gRPC Python protocol compiler plugin. DO NOT EDIT!
"""Client and server classes corresponding to protobuf-defined services."""
import grpc
import warnings

from app.grpc.datasource import datasource_pb2 as app_dot_grpc_dot_datasource_dot_datasource__pb2

GRPC_GENERATED_VERSION = '1.64.1'
GRPC_VERSION = grpc.__version__
EXPECTED_ERROR_RELEASE = '1.65.0'
SCHEDULED_RELEASE_DATE = 'June 25, 2024'
_version_not_supported = False

try:
    from grpc._utilities import first_version_is_lower
    _version_not_supported = first_version_is_lower(GRPC_VERSION, GRPC_GENERATED_VERSION)
except ImportError:
    _version_not_supported = True

if _version_not_supported:
    warnings.warn(
        f'The grpc package installed is at version {GRPC_VERSION},'
        + f' but the generated code in app/grpc/datasource/datasource_pb2_grpc.py depends on'
        + f' grpcio>={GRPC_GENERATED_VERSION}.'
        + f' Please upgrade your grpc module to grpcio>={GRPC_GENERATED_VERSION}'
        + f' or downgrade your generated code using grpcio-tools<={GRPC_VERSION}.'
        + f' This warning will become an error in {EXPECTED_ERROR_RELEASE},'
        + f' scheduled for release on {SCHEDULED_RELEASE_DATE}.',
        RuntimeWarning
    )


class DatasourceStub(object):
    """Missing associated documentation comment in .proto file."""

    def __init__(self, channel):
        """Constructor.

        Args:
            channel: A grpc.Channel.
        """
        self.InitTenant = channel.unary_unary(
                '/grpc.Datasource/InitTenant',
                request_serializer=app_dot_grpc_dot_datasource_dot_datasource__pb2.InitTenantRequest.SerializeToString,
                response_deserializer=app_dot_grpc_dot_datasource_dot_datasource__pb2.InitTenantResponse.FromString,
                _registered_method=True)
        self.CreateEnrichItem = channel.unary_unary(
                '/grpc.Datasource/CreateEnrichItem',
                request_serializer=app_dot_grpc_dot_datasource_dot_datasource__pb2.CreateEnrichItemRequest.SerializeToString,
                response_deserializer=app_dot_grpc_dot_datasource_dot_datasource__pb2.CreateEnrichItemResponse.FromString,
                _registered_method=True)
        self.CreatePluginDriver = channel.unary_unary(
                '/grpc.Datasource/CreatePluginDriver',
                request_serializer=app_dot_grpc_dot_datasource_dot_datasource__pb2.CreatePluginDriverRequest.SerializeToString,
                response_deserializer=app_dot_grpc_dot_datasource_dot_datasource__pb2.CreatePluginDriverResponse.FromString,
                _registered_method=True)
        self.CreatePresetPluginDriver = channel.unary_unary(
                '/grpc.Datasource/CreatePresetPluginDriver',
                request_serializer=app_dot_grpc_dot_datasource_dot_datasource__pb2.CreatePresetPluginDriverRequest.SerializeToString,
                response_deserializer=app_dot_grpc_dot_datasource_dot_datasource__pb2.CreatePluginDriverResponse.FromString,
                _registered_method=True)
        self.GetLLMConfigurations = channel.unary_unary(
                '/grpc.Datasource/GetLLMConfigurations',
                request_serializer=app_dot_grpc_dot_datasource_dot_datasource__pb2.GetLLMConfigurationsRequest.SerializeToString,
                response_deserializer=app_dot_grpc_dot_datasource_dot_datasource__pb2.GetLLMConfigurationsResponse.FromString,
                _registered_method=True)


class DatasourceServicer(object):
    """Missing associated documentation comment in .proto file."""

    def InitTenant(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def CreateEnrichItem(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def CreatePluginDriver(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def CreatePresetPluginDriver(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def GetLLMConfigurations(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')


def add_DatasourceServicer_to_server(servicer, server):
    rpc_method_handlers = {
            'InitTenant': grpc.unary_unary_rpc_method_handler(
                    servicer.InitTenant,
                    request_deserializer=app_dot_grpc_dot_datasource_dot_datasource__pb2.InitTenantRequest.FromString,
                    response_serializer=app_dot_grpc_dot_datasource_dot_datasource__pb2.InitTenantResponse.SerializeToString,
            ),
            'CreateEnrichItem': grpc.unary_unary_rpc_method_handler(
                    servicer.CreateEnrichItem,
                    request_deserializer=app_dot_grpc_dot_datasource_dot_datasource__pb2.CreateEnrichItemRequest.FromString,
                    response_serializer=app_dot_grpc_dot_datasource_dot_datasource__pb2.CreateEnrichItemResponse.SerializeToString,
            ),
            'CreatePluginDriver': grpc.unary_unary_rpc_method_handler(
                    servicer.CreatePluginDriver,
                    request_deserializer=app_dot_grpc_dot_datasource_dot_datasource__pb2.CreatePluginDriverRequest.FromString,
                    response_serializer=app_dot_grpc_dot_datasource_dot_datasource__pb2.CreatePluginDriverResponse.SerializeToString,
            ),
            'CreatePresetPluginDriver': grpc.unary_unary_rpc_method_handler(
                    servicer.CreatePresetPluginDriver,
                    request_deserializer=app_dot_grpc_dot_datasource_dot_datasource__pb2.CreatePresetPluginDriverRequest.FromString,
                    response_serializer=app_dot_grpc_dot_datasource_dot_datasource__pb2.CreatePluginDriverResponse.SerializeToString,
            ),
            'GetLLMConfigurations': grpc.unary_unary_rpc_method_handler(
                    servicer.GetLLMConfigurations,
                    request_deserializer=app_dot_grpc_dot_datasource_dot_datasource__pb2.GetLLMConfigurationsRequest.FromString,
                    response_serializer=app_dot_grpc_dot_datasource_dot_datasource__pb2.GetLLMConfigurationsResponse.SerializeToString,
            ),
    }
    generic_handler = grpc.method_handlers_generic_handler(
            'grpc.Datasource', rpc_method_handlers)
    server.add_generic_rpc_handlers((generic_handler,))
    server.add_registered_method_handlers('grpc.Datasource', rpc_method_handlers)


 # This class is part of an EXPERIMENTAL API.
class Datasource(object):
    """Missing associated documentation comment in .proto file."""

    @staticmethod
    def InitTenant(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(
            request,
            target,
            '/grpc.Datasource/InitTenant',
            app_dot_grpc_dot_datasource_dot_datasource__pb2.InitTenantRequest.SerializeToString,
            app_dot_grpc_dot_datasource_dot_datasource__pb2.InitTenantResponse.FromString,
            options,
            channel_credentials,
            insecure,
            call_credentials,
            compression,
            wait_for_ready,
            timeout,
            metadata,
            _registered_method=True)

    @staticmethod
    def CreateEnrichItem(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(
            request,
            target,
            '/grpc.Datasource/CreateEnrichItem',
            app_dot_grpc_dot_datasource_dot_datasource__pb2.CreateEnrichItemRequest.SerializeToString,
            app_dot_grpc_dot_datasource_dot_datasource__pb2.CreateEnrichItemResponse.FromString,
            options,
            channel_credentials,
            insecure,
            call_credentials,
            compression,
            wait_for_ready,
            timeout,
            metadata,
            _registered_method=True)

    @staticmethod
    def CreatePluginDriver(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(
            request,
            target,
            '/grpc.Datasource/CreatePluginDriver',
            app_dot_grpc_dot_datasource_dot_datasource__pb2.CreatePluginDriverRequest.SerializeToString,
            app_dot_grpc_dot_datasource_dot_datasource__pb2.CreatePluginDriverResponse.FromString,
            options,
            channel_credentials,
            insecure,
            call_credentials,
            compression,
            wait_for_ready,
            timeout,
            metadata,
            _registered_method=True)

    @staticmethod
    def CreatePresetPluginDriver(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(
            request,
            target,
            '/grpc.Datasource/CreatePresetPluginDriver',
            app_dot_grpc_dot_datasource_dot_datasource__pb2.CreatePresetPluginDriverRequest.SerializeToString,
            app_dot_grpc_dot_datasource_dot_datasource__pb2.CreatePluginDriverResponse.FromString,
            options,
            channel_credentials,
            insecure,
            call_credentials,
            compression,
            wait_for_ready,
            timeout,
            metadata,
            _registered_method=True)

    @staticmethod
    def GetLLMConfigurations(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(
            request,
            target,
            '/grpc.Datasource/GetLLMConfigurations',
            app_dot_grpc_dot_datasource_dot_datasource__pb2.GetLLMConfigurationsRequest.SerializeToString,
            app_dot_grpc_dot_datasource_dot_datasource__pb2.GetLLMConfigurationsResponse.FromString,
            options,
            channel_credentials,
            insecure,
            call_credentials,
            compression,
            wait_for_ready,
            timeout,
            metadata,
            _registered_method=True)
