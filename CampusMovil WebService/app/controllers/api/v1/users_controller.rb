class Api::V1::UsersController < ApplicationController
	skip_before_filter :verify_authenticity_token

	def create 
		@user = User.new user_params

		if @user.save
			render json: { success: true, user: @user }
		else 
			render json: { success: false, errors: @user.errors }
		end
	end

	private 
		def user_params
			params.require(:user).permit(:email, :username, :password)
		end
end
