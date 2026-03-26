
SCORE:  

The final score is calculated by the sum of the value of each advertisement played (won) divided
by the total ebucks spent, but no less than 30% of the provided budget.  

AUCTION MODEL:  

The model of the auction consists of three main components: Category, Video, and Viewer.  
With properties:
- Categories: Music, Sports, Kids, DIY, Video Games, ASMR, Beauty, Cooking, Finance  
- Video: Category, View count, Comment count  
- Viewer: subscribed, age, gender, “interests” (they map to the same categories above)  

The participants must choose a category they would be advertising on. The bots must send their
chosen category as the first line.  

At the end of each video bidding round, you Lose or Win [space] cost in ebucks in case of a win.
E.g. “W 11” or “L”  

Provide a starting and max bid, e.g., 2 5.


INIT:  
The bot starts with a chosen category (mentioned above). It happens once only, and it
cannot be changed.

LOOP:  
The bidding platform sends information about the video and the viewer. The bot should
read it through the standard input.

The bot sends a start and a max bid in ebucks (no fractions). Separated by a white space
(space or tab \t) limited to 40ms  

The bidding platform sends a Win or Lose to each participant, along with the ebucks
spent to the winner (only L when loss, W [amount] when won)

Every 100 rounds of videos and bids, the bidding platform sends a summary of the
points accumulated for those 100 videos, along with ebucks spent. The format is: S
{points} {ebucks}.
Example:
S 1289 199  

The process continues until a set number of videos (greater than 100 000) have been
played or fewer than 10% of participants have unspent ebucks.

BOT STRUCTURE:  

The bot input follows the format of “{field}=value(,)...\n”
Fields:
- video.category - main category for the video, (loosely) matching the category of
  the advertising is more likely to drive engagement
- video.viewCount - video count, the view count itself is not necessarily indicative
  for the value of shown advertisement, the distribution of view count in videos
  follows the power law, with view counts ranging up to 97 millions. More info: [0]
- video.commentCount - a higher ratio of commentCount/viewCount suggests
  higher engagement with the video
- viewer.subscribed - Y / N Whether the viewer is subscribed to the channel,
  subscriptions result in higher value
- viewer.age Age bracket, possible values: 13-17, 18-24, 25-34, 35-44, 45-54, 55+
- viewer.gender M / F Viewer gender, to simplify the algorithm, the gender is
  always present
- viewer.interests 1–3 interests - the categories are picked from the videos
  category list, semicolon-separated, ordered by relevance

EXAMPLE VIDEO  
```
video.viewCount=12345,video.category=Kids,video.commentCount=987,view
er.subscribed=Y,viewer.age=18-24,viewer.gender=F,viewer.interests=Vid
eo Games;Music
```

[0]: The relationship between view count and bracket value is not monotonic: it has peaks, dips,
and rebounds that reflect real advertising economics. A niche video can be worth more per
impression than a viral one. Discovering this value curve through experimentation is part of the
challenge.