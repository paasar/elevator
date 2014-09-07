import web
import json
from logic import decide_which_floor_to_go

urls = (
  '/', 'index'
)

class index:
  def GET(self):
    return "I'm a little Python elevator. Please POST state here to get where I want to go."

  def POST(self):
    body = web.data()
    state = json.loads(body)
    print "PlayerState:"
    print json.dumps(state, indent=2)

    return '{"go-to": ' + str(decide_which_floor_to_go(state)) + '}'

if __name__ == "__main__":
    app = web.application(urls, globals())
    app.run()
