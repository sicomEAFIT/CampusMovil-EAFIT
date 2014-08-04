class AddUserIdToMarkers < ActiveRecord::Migration
  def change
    add_column :markers, :user_id, :integer

    add_index :markers, ['user_id'], name: 'index_user_id'
  end
end
