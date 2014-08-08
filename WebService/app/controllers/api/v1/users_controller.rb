module Api
  module V1
    class UsersController < BaseController
      skip_before_filter :verify_authenticity_token
      before_filter :restrict_access, except:[:create]

      def create
        user = User.new user_params

        if user.save
          render json: { success: true, user: user }
        else
          render json: { success: false, errors: user.errors }
        end
      end

      def comment
        comment = Comment.new comment_params
        comment.user = @user

        if comment.save
          render json: { success: true }
        else
          render json: { success: false, errors: comment.errors }
        end
      end

      private
      def comment_params
        params.require(:comment).permit(:message)
      end

      def user_params
        params.require(:user).permit(:email, :username, :password)
      end
    end
  end
end

