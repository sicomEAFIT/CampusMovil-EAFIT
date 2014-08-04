class CreateUsers < ActiveRecord::Migration
  def change
    create_table :users do |t|
      t.string :email
      t.string :username
      t.string :password
      t.string :salt
      t.boolean :admin, null: false, default: false

      t.timestamps
    end

    add_index :users, ['username'], name: 'index_username', unique: true
  end
end
