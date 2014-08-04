class CreateApiAuths < ActiveRecord::Migration
  def change
    create_table :api_auths do |t|
      t.string :token
      t.datetime :expires
      t.integer :user_id

      t.timestamps
    end

    add_index :api_auths, ['token'], name: 'index_token', unique: true
  end
end
