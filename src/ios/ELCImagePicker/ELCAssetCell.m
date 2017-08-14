//
//  AssetCell.m
//
//  Created by ELC on 2/15/11.
//  Copyright 2011 ELC Technologies. All rights reserved.
//

#import "ELCAssetCell.h"
#import "ELCAsset.h"
#import "Donkeyfont.h"

@interface ELCAssetCell ()

@property (nonatomic, strong) NSArray *rowAssets;
@property (nonatomic, strong) NSMutableArray *imageViewArray;
@property (nonatomic, strong) NSMutableArray *checkIconArray;
@property (nonatomic, strong) NSMutableArray *circleIconArray;

@end

static const float gThumbWidth = 75.0f;
static const float gThumbHeight = 75.0f;
static const float gTopBuffer = 2.0f;
static const float gIconLeftBuffer = 5.0f;
static const float gIconTopBuffer = 5.0f;
static const unsigned int gRowMax = 4;

@implementation ELCAssetCell

//Using auto synthesizers

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:UITableViewCellStyleDefault reuseIdentifier:reuseIdentifier];
	if (self) {
        UITapGestureRecognizer *tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(cellTapped:)];
        [self addGestureRecognizer:tapRecognizer];
        
        NSMutableArray *mutableArray = [[NSMutableArray alloc] initWithCapacity:gRowMax];
        self.imageViewArray = mutableArray;
        
        NSMutableArray *checkIconArray = [[NSMutableArray alloc] initWithCapacity:gRowMax];
        self.checkIconArray = checkIconArray;
        
        NSMutableArray *circleIconArray = [[NSMutableArray alloc] initWithCapacity:gRowMax];
        self.circleIconArray = circleIconArray;
	}
	return self;
}

- (void)setAssets:(NSArray *)assets
{
    self.rowAssets = assets;
	for (UIImageView *view in _imageViewArray) {
        [view removeFromSuperview];
	}

    for (UILabel *view in _checkIconArray) {
        [view removeFromSuperview];
    }
    
    for (UILabel *view in _circleIconArray) {
        [view removeFromSuperview];
    }

    UIFont *donkeyFont = [UIFont fontWithName:@"DonkeyFont" size:25];
    UILabel *checkLabel = nil;
    UILabel *circleLabel = nil;
    CGSize overlaySize;
    for (int i = 0; i < [_rowAssets count]; ++i) {

        ELCAsset *asset = [_rowAssets objectAtIndex:i];

        if (i < [_imageViewArray count]) {
            UIImageView *imageView = [_imageViewArray objectAtIndex:i];
            imageView.image = [UIImage imageWithCGImage:asset.asset.thumbnail];
            overlaySize = CGSizeMake(CGRectGetWidth(imageView.frame), CGRectGetHeight(imageView.frame));
        } else {
            UIImageView *imageView = [[UIImageView alloc] initWithImage:[UIImage imageWithCGImage:asset.asset.thumbnail]];
            [_imageViewArray addObject:imageView];
            overlaySize = CGSizeMake(CGRectGetWidth(imageView.frame), CGRectGetHeight(imageView.frame));
        }

        if (i < [_checkIconArray count]) {
            UILabel *circleLabel = [_circleIconArray objectAtIndex:i];
            UILabel *checkLabel = [_checkIconArray objectAtIndex:i];
            [self selectThumbnail:checkLabel circleLabel:circleLabel selected:asset.selected];
        } else {
            checkLabel = [[UILabel alloc] init];
            checkLabel.textColor = [UIColor whiteColor];
            checkLabel.font = donkeyFont;
            checkLabel.textAlignment = NSTextAlignmentLeft;
            [_checkIconArray addObject:checkLabel];
            
            circleLabel = [[UILabel alloc] init];
            circleLabel.textColor = [UIColor whiteColor];
            circleLabel.font = donkeyFont;
            circleLabel.textAlignment = NSTextAlignmentLeft;
            [_circleIconArray addObject:circleLabel];
            
            [self selectThumbnail:checkLabel circleLabel:circleLabel selected:asset.selected];
        }
    }
}

- (void)cellTapped:(UITapGestureRecognizer *)tapRecognizer
{
    CGPoint point = [tapRecognizer locationInView:self];
    CGFloat totalWidth = self.rowAssets.count * gThumbWidth + (self.rowAssets.count - 1) * gRowMax;
    CGFloat startX = (self.bounds.size.width - totalWidth) / 2;
    
	CGRect frame = CGRectMake(startX, gTopBuffer, gThumbWidth, gThumbHeight);
	
    for (int i = 0; i < [_rowAssets count]; ++i) {

        if (CGRectContainsPoint(frame, point)) {
            ELCAsset *asset = [_rowAssets objectAtIndex:i];
            asset.selected = !asset.selected;
            UILabel *checkLabel = [_checkIconArray objectAtIndex:i];
            UILabel *circleLabel = [_circleIconArray objectAtIndex:i];
            [self selectThumbnail:checkLabel circleLabel:circleLabel selected:asset.selected];
            break;
        }
        frame.origin.x = frame.origin.x + frame.size.width + gRowMax;
    }
}

- (void)layoutSubviews
{    
    CGFloat totalWidth = self.rowAssets.count * gThumbWidth + (self.rowAssets.count - 1) * gRowMax;
    CGFloat startX = (self.bounds.size.width - totalWidth) / 2;
    
	CGRect frame = CGRectMake(startX, gTopBuffer, gThumbWidth, gThumbHeight);
	
	for (int i = 0; i < [_rowAssets count]; ++i) {
		UIImageView *imageView = [_imageViewArray objectAtIndex:i];
		[imageView setFrame:frame];
		[self addSubview:imageView];
        
        UILabel *checkLabel = [_checkIconArray objectAtIndex:i];
        UILabel *circleLabel = [_circleIconArray objectAtIndex:i];
        CGRect iconFrame = CGRectMake(frame.origin.x + gIconLeftBuffer,
                                         frame.origin.y + gIconTopBuffer,
                                         frame.size.width,
                                         frame.size.height / 3);
        [checkLabel setFrame:iconFrame];
        [circleLabel setFrame:iconFrame];
        [self addSubview:circleLabel];
        
        [self addSubview:checkLabel];
        
        
		
		frame.origin.x = frame.origin.x + frame.size.width + gRowMax;
	}
}

- (void)selectThumbnail:(UILabel*)checkLabel circleLabel:(UILabel*)circleLabel selected:(bool)selected {
    checkLabel.text = selected ? ICON_ZP_V2_GALLERY_IMAGE_SELECTED : ICON_ZP_V2_GALLERY_IMAGE_UNSELECTED;
    checkLabel.textColor = selected ? [UIColor colorWithRed:22.0f / 255.0f
                                                      green:157 / 255.0f
                                                       blue:217.0f / 255.0f
                                                      alpha:1.0] : [UIColor whiteColor];
    
    circleLabel.text = ICON_ZP_V2_CIRCLE;
    circleLabel.textColor = [UIColor whiteColor];
    circleLabel.alpha = selected ? 1.0f : 0.0f;
}


@end
