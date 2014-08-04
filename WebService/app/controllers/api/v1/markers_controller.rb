module Api
  module V1
    class MarkersController < BaseController
      respond_to :json

      skip_before_filter :verify_authenticity_token
      before_filter :restrict_access
      before_action :restrict_standard_user, only: [:create, :destroy, :update]
      before_action :set_marker, only: [:update, :destroy]

      def index
        markers = Marker.where('(title LIKE :q OR subtitle LIKE :q)',
                               q: '%#{params[:search]}%')

        if markers.any?
          respond_with markers
        else
          respond_with Marker.all
        end
      end

      def update
        sender = {
            success: true,
            marker: nil,
            error: nil
        }

        @marker.update marker_params

        sender[:success] = !@marker.errors.any?
        sender[:marker] = @marker
        sender[:error] = @marker.errors

        render json: sender

      end

      def destroy
        if @marker.destroy
          render json: { success: true }
        else
          render json: { success: false }
        end
      end

      def show
        render json: Marker.find(params[:id])
      end

      def create
        @marker = Marker.new(marker_params)

        if @marker.save
          render json: @marker
        else
          render json: { success: false, error: @marker.errors }
        end
      end

      private
        def marker_params
          params.require(:marker).permit(:title, :subtitle, :latitude, :longitude)
        end

        def set_marker
          @marker = Marker.find params[:id]
        end
    end
  end
end

