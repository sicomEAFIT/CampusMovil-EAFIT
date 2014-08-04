class ApiAuth < ActiveRecord::Base
  belongs_to :user

  before_save :generate_token

  def self.authenticate(username, password)
    user = User.authenticate username, password

    if user
      token = ApiAuth.new

      if ApiAuth.exists? user: user
        token = ApiAuth.find_by user: user
      end

      token.user_id = user.id
      if token.save
        return token
      end
    end
  end

  def valid_token?
    self.expires >= DateTime.now
  end

  def rebuild_token
    generate_token 15
  end

  private
    def generate_token(timelife = 30)
      begin
        self.token = SecureRandom.hex 256
      end while ApiAuth.exists? token: self.token

      self.expires = DateTime.now + timelife
    end
end
