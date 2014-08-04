require 'digest'

class User < ActiveRecord::Base
  has_many :markers

  validates_presence_of [:email, :password, :username]

  validates :username,
            uniqueness: true

  validates :password,
            confirmation: false

  validates :email,
            presence: true
            #format: { with: /[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}/ }

  before_save :encrypt_password
  before_save :formatter

  def self.authenticate(username, password)
    user = User.find_by username: username

    return unless user # false si no esta instanciado user
    return user if user.password == User.encrypt(password, user.salt)
  end

  def self.encrypt(password, salt)
    Digest::SHA2.hexdigest "#{password} => [#{salt}]"
  end

  def update_password(password)
    self.salt = generate_salt
    self.password = User.encrypt password, self.salt
  end

  private
    def encrypt_password
      if self.new_record?
        self.salt = generate_salt
        self.password = User.encrypt self.password, self.salt
      end
    end

    def generate_salt
      Digest::SHA2.hexdigest "#{SecureRandom.hex 8} #{Time.now.utc}"
    end

    def formatter

    end
end
