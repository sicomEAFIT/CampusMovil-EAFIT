module Api
  module V1
    class AuthController < BaseController
      skip_before_filter :verify_authenticity_token
      before_filter :restrict_access, only: [:update, :destroy]
      before_action :user_context, only: [:update, :destroy]

      def create
        auth = ApiAuth.authenticate auth_params[:username], auth_params[:password]

        if auth
          render_token true, 200, auth
        else
          render_token false, 401, nil, 'Invalid username or password'
        end
      end

      def update
        @auth.rebuild_token

        if @auth.save
          render_token true, 200, @auth
        else
          render_token false, 401, nil, @auth.error
        end
      end

      def destroy
        if @auth.destroy
          render json: {success:true}
        else
          render json: {success: false, code: 401, error: @auth.error}, status: 401
        end
      end

      private
        def auth_params
          params.require(:user).permit(:username, :password)
        end

        def render_token(success, code, auth, error = nil)
          token = {
              token: auth.token,
              expires: auth.expires
          } if auth else nil

          render json: {
              success:success,
              code:code,
              auth: token,
              error:error
          }, status: code
        end

        def user_context
          @auth = ApiAuth.find_by user_id: @user
        end
    end
  end
end
