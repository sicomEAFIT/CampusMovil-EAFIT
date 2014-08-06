class AddCategoryToMarkers < ActiveRecord::Migration
  def change
    add_column :markers, :category, :string
  end
end
