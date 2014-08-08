module Api
  module V1
    class BaseController < ApplicationController
      respond_to :json

      rescue_from ActiveRecord::RecordNotFound, with: :not_found
      rescue_from ActionController::RoutingError, with: :not_found

      private
        def restrict_access
          # metodo inseguro haciendo uso de GET PARAMS para auth /controller/action?auth=xxxxxxx
          # INSEGURO
          if params[:auth] && !params[:auth].empty?
            auth = ApiAuth.find_by token: params[:auth]

            if auth && auth.valid_token?
              @user = auth.user

              return true
            end
          end

          # Autenticacion haciendo uso de HTTP Header: Authentication: Token token=xxxxxxxxx
          # metodo seguro de Auth manual via header
          authenticate_or_request_with_http_token do | token, options |
            auth = ApiAuth.find_by token: token

            if auth && auth.valid_token?
              @user = auth.user

              return true
            else
              unauthorized_response
              return false
            end
          end

          unauthorized_response
          return false
        end

        def restrict_standard_user
          # code: 401, error: Authentication only available for administrators

          unauthorized_response unless @user.admin?
          return
        end

        def not_found
          @errors = { success: false, code: 404, error: 'Content/Page not found' }
        end

        def unauthorized_response
          @errors = { success: false, code: 401,
                     error: 'Not authorized: Token/Session is invalid or don\'t have permissions' }
        end
    end
  end
end

